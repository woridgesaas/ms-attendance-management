package org.woridge.ms_attendance_management.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestAdminResponse;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestListResponse;
import org.woridge.ms_attendance_management.enumeration.LeaveRequestDecision;
import org.woridge.ms_attendance_management.exception.leave.*;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.mapper.LeaveRequestMapper;

import org.woridge.ms_attendance_management.mapper.LeaveMapper;
import org.woridge.ms_attendance_management.messaging.publisher.LeaveHandleEventPublisher;
import org.woridge.ms_attendance_management.model.*;
import org.woridge.ms_attendance_management.repository.*;
import org.woridge.ms_attendance_management.service.LeaveService;
import org.woridge.ms_attendance_management.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final HolidayRepository holidayRepository;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final ValidationRepository validationRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveHandleEventPublisher leaveHandleEventPublisher;
    /**
     * Processes the approval of a leave request.
     * <p>
     * This method performs the following steps:
     * 1. Validates the existence of the leave request.
     * 2. Checks if a mandatory justification is present based on the leave type.
     * 3. Calculates the effective business days to deduct (excluding weekends and holidays).
     * 4. Verifies if the employee has a sufficient leave balance for the specific year.
     * 5. Deducts the days from the balance and records the individual leave days.
     * 6. Persists a validation record for audit purposes.
     * </p>
     *
     * @param leaveRequestId The unique identifier of the leave request to approve.
     * @param managerCin     The CIN of the manager performing the approval.
     * @throws LeaveRequestException If the request or balance is not found.
     * @throws JustificationRequiredException If a required justification is missing.
     * @throws InsufficientLeaveBalanceException If the remaining balance is lower than requested days.
     * @throws UserNotFoundException If the manager cannot be identified.
     */

    @Transactional
    public LeaveRequest approveLeave(UUID leaveRequestId , String managerCin){
        // 1. Retrieve the leave request
        var leaveRequest = this.leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(()-> new LeaveRequestException("Leave Request not found"));

        // 2. Check for justification if required by type
        if(leaveRequest.getLeaveType().requiresJustification()
                && leaveRequest.getJustification() == null ){
            throw new JustificationRequiredException("No justification found");
        }

        // 3. Calculation of effective leave days (Filtering weekends - holidays)
        LocalDate current = leaveRequest.getStartDate();
        int daysToDeduct = 0;
        if (leaveRequest.getLeaveDays() == null) {
            leaveRequest.setLeaveDays(new ArrayList<>());
        } else {
            leaveRequest.getLeaveDays().clear();
        }
        List<LeaveDay> leaveDays = new ArrayList<>();
        while(!current.isAfter(leaveRequest.getEndDate())){
            if(!DateUtils.isWeekend(current) &&
               !this.holidayRepository.existsByDate(current)){
                    daysToDeduct++;
                new LeaveDay();
                LeaveDay day = LeaveDay
                            .builder()
                            .dateOfDay(current)
                            .leaveRequest(leaveRequest)
                            .build();
                leaveRequest.getLeaveDays().add(day);

            }
            current=current.plusDays(1);
        }


        // 4. Balance management
        int yearOfBalance = leaveRequest.getCreatedAt().getYear();
        Balance employeBalance = balanceRepository.findByUserIdAndYear(
                        leaveRequest.getUser().getId(), yearOfBalance)
                .orElseThrow(() -> new LeaveRequestException("Balance for year " + yearOfBalance + " not found"));

        int remainingSold = employeBalance.getRemainingBalance();
        if( remainingSold< daysToDeduct)
            throw new InsufficientLeaveBalanceException("insufficient balance");
        employeBalance.setRemainingBalance(remainingSold - daysToDeduct);

        // 5. Validation record
        Optional<User> manager = Optional.of(userRepository.findByCin(managerCin))
                .orElseThrow(()-> new UserNotFoundException("User with the following cin not found : " + managerCin));

        Validation validation = Validation.builder()
                .decision(LeaveRequestDecision.APPROVED)
                .decisionDate(LocalDateTime.now())
                .performedBy(manager.get())
                .leaveRequest(leaveRequest)
                .build();
        leaveRequest.setValidation(validation);
        this.validationRepository.save(validation);

        //6. Notify employe
        this.leaveHandleEventPublisher.publish(
               LeaveMapper.toPublishEvent(leaveRequest , remainingSold)
        );
        return leaveRequest;

    }
    @Transactional
    public List<LeaveRequestAdminResponse> getPendingLeaves(String managerCin, String role) {

        if (!"MANAGER".equals(role)) {
            throw new OnlyManagerAccessException("Only managers can access pending leaves");
        }

        User manager = userRepository.findByCin(managerCin)
                .orElseThrow(() -> new UserNotFoundException("Manager not found"));

        return leaveRequestRepository
                .findPendingRequestsByDepartment(manager.getDepartmentId())
                .stream()
                .map(l -> leaveRequestMapper.toAdminResponse(l, role))
                .toList();
    }
    @Transactional
    public LeaveRequest rejectLeave(UUID leaveRequestId, String managerCin) {

        // 1. Get manager
        User manager = userRepository.findByCin(managerCin)
                .orElseThrow(() -> new UserNotFoundException("Manager not found"));

        // 2. Get leave
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave not found"));

        // 3. Check already processed
        if (leaveRequest.getValidation() != null) {
            throw new LeaveRequestException("Leave already processed");
        }

        // 4. Create validation
        Validation validation = Validation.builder()
                .leaveRequest(leaveRequest)
                .performedBy(manager)
                .decision(LeaveRequestDecision.REJECTED)
                .decisionDate(LocalDateTime.now())
                .build();

        //  IMPORTANT
        leaveRequest.setValidation(validation);

        validationRepository.save(validation);

        return leaveRequest;
    }

}
