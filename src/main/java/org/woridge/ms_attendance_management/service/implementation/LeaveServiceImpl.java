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
import org.woridge.ms_attendance_management.dto.leave.CalendarLeaveResponse;
import java.time.YearMonth;

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

    @Transactional
    public LeaveRequest approveLeave(UUID leaveRequestId, String managerCin) {

        // 1. Récupérer la demande
        var leaveRequest = this.leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestException("Leave Request not found"));

        // 2. Vérifier justification si obligatoire selon le type
        if (leaveRequest.getLeaveType().requiresJustification()
                && leaveRequest.getJustification() == null) {
            throw new JustificationRequiredException("No justification found");
        }

        // 3. Calculer les jours ouvrables (sans weekends ni jours fériés)
        LocalDate current = leaveRequest.getStartDate();
        int daysToDeduct = 0;

        if (leaveRequest.getLeaveDays() == null) {
            leaveRequest.setLeaveDays(new ArrayList<>());
        } else {
            leaveRequest.getLeaveDays().clear();
        }

        while (!current.isAfter(leaveRequest.getEndDate())) {
            if (!DateUtils.isWeekend(current) &&
                    !this.holidayRepository.existsByDate(current)) {
                daysToDeduct++;
                LeaveDay day = LeaveDay.builder()
                        .dateOfDay(current)
                        .leaveRequest(leaveRequest)
                        .build();
                leaveRequest.getLeaveDays().add(day);
            }
            current = current.plusDays(1);
        }

        // 4. Vérifier et déduire le solde
        int yearOfBalance = leaveRequest.getCreatedAt().getYear();
        Balance employeBalance = balanceRepository.findByUserIdAndYear(
                        leaveRequest.getUser().getId(), yearOfBalance)
                .orElseThrow(() -> new LeaveRequestException(
                        "Balance for year " + yearOfBalance + " not found"));

        int remainingSold = employeBalance.getRemainingBalance();
        if (remainingSold < daysToDeduct) {
            throw new InsufficientLeaveBalanceException("Insufficient balance");
        }
        employeBalance.setRemainingBalance(remainingSold - daysToDeduct);

        // 5. Créer la validation
        Optional<User> manager = Optional.of(userRepository.findByCin(managerCin))
                .orElseThrow(() -> new UserNotFoundException(
                        "User with the following cin not found : " + managerCin));

        Validation validation = Validation.builder()
                .decision(LeaveRequestDecision.APPROVED)
                .decisionDate(LocalDateTime.now())
                .performedBy(manager.get())
                .leaveRequest(leaveRequest)
                .build();
        leaveRequest.setValidation(validation);
        this.validationRepository.save(validation);

        // 6. Notifier l'employé via RabbitMQ
        this.leaveHandleEventPublisher.publish(
                LeaveMapper.toPublishEvent(leaveRequest, remainingSold)
        );

        return leaveRequest;
    }

    @Transactional
    public List<LeaveRequestAdminResponse> getPendingLeaves(String managerCin, String role) {

        // ✅ RH voit TOUTES les demandes de tous les départements
        if ("HR".equals(role) || "RH".equals(role)) {
            return leaveRequestRepository
                    .findAllPendingRequests()
                    .stream()
                    .map(l -> leaveRequestMapper.toAdminResponse(l, role))
                    .toList();
        }

        // ✅ MANAGER voit seulement son département
        if (!"MANAGER".equals(role)) {
            throw new OnlyManagerAccessException(
                    "Only managers or HR can access pending leaves");
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

        // 1. Récupérer le manager
        User manager = userRepository.findByCin(managerCin)
                .orElseThrow(() -> new UserNotFoundException("Manager not found"));

        // 2. Récupérer la demande
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave not found"));

        // 3. Vérifier si déjà traitée
        if (leaveRequest.getValidation() != null) {
            throw new LeaveRequestException("Leave already processed");
        }

        // 4. Créer la validation REJECTED
        Validation validation = Validation.builder()
                .leaveRequest(leaveRequest)
                .performedBy(manager)
                .decision(LeaveRequestDecision.REJECTED)
                .decisionDate(LocalDateTime.now())
                .build();

        leaveRequest.setValidation(validation);
        validationRepository.save(validation);

        return leaveRequest;
    }

    @Transactional
    public List<CalendarLeaveResponse> getApprovedLeavesForCalendar(
            String userCin, String role, int year, int month, UUID departmentIdFilter) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        List<LeaveRequest> leaves;

        // RH voit tous les départements (ou un filtré)
        if ("HR".equals(role) || "RH".equals(role)) {
            if (departmentIdFilter != null) {
                leaves = leaveRequestRepository
                        .findApprovedLeavesByDepartmentAndMonth(
                                departmentIdFilter, startOfMonth, endOfMonth);
            } else {
                leaves = leaveRequestRepository
                        .findAllApprovedLeavesByMonth(startOfMonth, endOfMonth);
            }
        }
        // MANAGER → toujours son département
        else if ("MANAGER".equals(role)) {
            User manager = userRepository.findByCin(userCin)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));
            leaves = leaveRequestRepository
                    .findApprovedLeavesByDepartmentAndMonth(
                            manager.getDepartmentId(), startOfMonth, endOfMonth);
        }
        else {
            throw new OnlyManagerAccessException(
                    "Only managers or HR can view the leave calendar");
        }

        return leaves.stream()
                .map(lr -> CalendarLeaveResponse.builder()
                        .leaveId(lr.getId())
                        .employeeId(lr.getUser().getId())
                        .employeeName(lr.getUser().getFullName())
                        .departmentId(lr.getUser().getDepartmentId())
                        .startDate(lr.getStartDate())
                        .endDate(lr.getEndDate())
                        .leaveType(lr.getLeaveType())
                        .build())
                .toList();
    }
    public List<UUID> getDepartmentsWithApprovedLeaves() {
        return leaveRequestRepository.findDistinctDepartmentIdsWithApprovedLeaves();
    }
}