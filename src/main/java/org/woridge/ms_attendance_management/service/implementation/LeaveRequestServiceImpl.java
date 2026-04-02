package org.woridge.ms_attendance_management.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.dto.leave.CreateLeaveRequest;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestFilter;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestListResponse;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestResponse;
import org.woridge.ms_attendance_management.exception.leave.InsufficientLeaveBalanceException;
import org.woridge.ms_attendance_management.exception.leave.InvalidLeaveDatesException;
import org.woridge.ms_attendance_management.exception.leave.JustificationRequiredException;
import org.woridge.ms_attendance_management.exception.leave.LeaveRequestException;
import org.woridge.ms_attendance_management.mapper.LeaveRequestMapper;
import org.woridge.ms_attendance_management.model.Balance;
import org.woridge.ms_attendance_management.model.Justification;
import org.woridge.ms_attendance_management.model.LeaveRequest;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.repository.BalanceRepository;
import org.woridge.ms_attendance_management.repository.JustificationRepository;
import org.woridge.ms_attendance_management.repository.LeaveRequestRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.LeaveRequestService;
import org.woridge.ms_attendance_management.service.MinioStorageService;
import org.woridge.ms_attendance_management.utils.DateUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final BalanceRepository balanceRepository;
    private final JustificationRepository justificationRepository;
    private final MinioStorageService minioStorageService;
    private final UserRepository userRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    public LeaveRequestResponse createLeaveRequest(
            String userCin,
            String data,
            MultipartFile justificationFile){

        // 0. Désérialisation via mapper
        CreateLeaveRequest dto = leaveRequestMapper.toCreateLeaveRequest(data);

        // 1. Récupérer l'utilisateur
        User user = userRepository.findByCin(userCin)
                .orElseThrow(() -> new LeaveRequestException(
                        "Utilisateur introuvable pour le CIN : " + userCin));

        // 2. Validation des dates
        validateDates(dto.getStartDate(), dto.getEndDate());

        // 3. Calcul des jours
        int numberOfDays = DateUtils.calculateBusinessDays(dto.getStartDate(), dto.getEndDate());

        // 4. Vérification justificatif
        if (dto.getLeaveType().requiresJustification()) {
            if (justificationFile == null || justificationFile.isEmpty()) {
                throw new JustificationRequiredException("justification file is required");
            }
        }

        // 5. Vérification solde
        Balance balance = getOrThrowBalance(user.getId());
        if (balance.getRemainingBalance() < numberOfDays) {
            throw new InsufficientLeaveBalanceException("votre solde est insuffisant");
        }

        // 6. Mapping DTO → Entity
        LeaveRequest leaveRequest = leaveRequestMapper.toLeaveRequest(dto, user);
        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // 7. Upload justificatif
        String justificationPath = null;
        if (justificationFile != null && !justificationFile.isEmpty()) {
            justificationPath = minioStorageService.uploadJustification(
                    justificationFile, leaveRequest.getId());

            Justification justification = Justification.builder()
                    .path(justificationPath)
                    .leaveRequest(leaveRequest)
                    .build();

            justificationRepository.save(justification);
        }

        // 8. Mapping Entity → Response DTO
        return leaveRequestMapper.toLeaveRequestResponse(
                leaveRequest,
                numberOfDays,
                balance.getRemainingBalance(),
                justificationPath
        );
    }
    @Override
    public List<LeaveRequestListResponse> getAllLeaveRequests(
            String userCin,
            LeaveRequestFilter filter) {

        User user = userRepository.findByCin(userCin)
                .orElseThrow(() -> new LeaveRequestException(
                        "Utilisateur introuvable pour le CIN : " + userCin));

        return leaveRequestRepository
                .findAllByFilters(
                        user.getId(),
                        filter.getLeaveType(),
                        filter.getStatus(),
                        filter.getStartDate(),
                        filter.getEndDate()
                )
                .stream()
                .map(leaveRequestMapper::toListResponse)
                .collect(Collectors.toList());
    }

    // --- Méthodes privées ---

    private void validateDates(LocalDate start, LocalDate end) {
        if (!end.isAfter(start)) {
            throw new InvalidLeaveDatesException("leave date must be after start date");
        }
    }


    private Balance getOrThrowBalance(UUID userId) {
        int currentYear = LocalDate.now().getYear();
        return balanceRepository.findByUserIdAndYear(userId, currentYear)
                .orElseThrow(() -> new LeaveRequestException(
                        "Aucun solde de congé trouvé pour l'année " + currentYear));
    }
}