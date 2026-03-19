package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woridge.ms_attendance_management.dto.absence.AbsenceProcessingRequest;
import org.woridge.ms_attendance_management.dto.absence.AbsenceResponse;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.enumeration.AlertResult;
import org.woridge.ms_attendance_management.exception.absence.AbsenceNotFoundException;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.handler.AbsenceHandler;
import org.woridge.ms_attendance_management.mapper.AbsenceMapper;
import org.woridge.ms_attendance_management.model.Absence;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.propreties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.AbsenceRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.AbsenceProcessingService;
import org.woridge.ms_attendance_management.service.AlertService;
import org.woridge.ms_attendance_management.service.ConsecutiveAbsenceService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AbsenceProcessingServiceImpl implements AbsenceProcessingService {

    private final AbsenceRepository      absenceRepository;
    private final UserRepository         userRepository;
    private final AbsenceHandler         absenceHandlerChain;
    private final ConsecutiveAbsenceService consecutiveAbsenceService;
    private final AlertService           alertService;
    private final BusinessProperties     businessProperties;
    private final AbsenceMapper          absenceMapper;

    @Override
    @Transactional
    public AbsenceResponse process(AbsenceProcessingRequest request) {

        // ── 1. Vérification utilisateur ───────────────────────────
        isUserExists(request.userId());

        // ── 2. Détection présence ─────────────────────────────────
        if (request.workedHours() > 0) {
            return null;
        }
        // ── 3. Vérification état final ────────────────────────────
        // Si déjà UNJUSTIFIED_FINAL → on retourne directement sans exception
        var existingAbsence = absenceRepository
                .findByUserIdAndAbsenceDate(request.userId(), request.date());

        if (existingAbsence.isPresent() && existingAbsence.get().isFinal()) {
            return absenceMapper.toAbsenceResponse(existingAbsence.get());
        }

        // ── 4. Classification via Chain of Responsibility ─────────
        AbsenceType absenceType = absenceHandlerChain
                .handle(request.userId(), request.date());

        // ── 5. Calcul jours consécutifs ───────────────────────────
        int consecutiveDays = absenceType.isJustified()
                ? 0
                : consecutiveAbsenceService.calculate(request.userId(), request.date());

        // ── 6. Vérification seuil critique → UNJUSTIFIED_FINAL ────
        boolean isFinal = consecutiveDays >= businessProperties.getCeoAlertThreshold()
                && absenceType.isUnjustified();

        if (isFinal) {
            absenceType = AbsenceType.UNJUSTIFIED_FINAL;

        }

        // ── 7. Persistance ────────────────────────────────────────
        Absence absence = saveOrUpdate(
                request.userId(),
                request.date(),
                absenceType,
                isFinal,
                consecutiveDays
        );

        // ── 8. Évaluation alertes ─────────────────────────────────
        if (!absenceType.isJustified()) {
            AlertResult alertResult = alertService.evaluate(request.userId(), consecutiveDays);
        }

        // ── 9. Mapping entity → DTO ───────────────────────────────
        return absenceMapper.toAbsenceResponse(absence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceResponse> getAbsencesByUser(UUID userId) {
        isUserExists(userId);

        return absenceMapper.toAbsenceResponseList(
                absenceRepository.findByUserIdOrderByAbsenceDateDesc(userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AbsenceResponse getAbsenceByUserAndDate(UUID userId, LocalDate date) {
        isUserExists(userId);

        Absence absence = absenceRepository
                .findByUserIdAndAbsenceDate(userId, date)
                .orElseThrow(() -> new AbsenceNotFoundException(
                        "No absence found for user [" + userId + "] on [" + date + "]"
                ));

        return absenceMapper.toAbsenceResponse(absence);
    }

    private void isUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    "User not found with id: " + userId
            );
        }
    }

//    private void validateNotFinalState(UUID userId, LocalDate date) {
//        if (absenceRepository.existsByUserIdAndAbsenceDateAndIsFinalTrue(userId, date)) {
//            throw new AbsenceFinalStateException(
//                    "Absence of user [" + userId + "] on ["
//                            + date + "] is UNJUSTIFIED_FINAL and cannot be modified."
//            );
//        }
//    }

    private Absence saveOrUpdate(UUID userId, LocalDate date, AbsenceType type,
                                 boolean isFinal, int consecutiveDays) {

        User userRef = userRepository.getReferenceById(userId);

        Absence absence = absenceRepository
                .findByUserIdAndAbsenceDate(userId, date)
                .orElse(Absence.builder()
                        .user(userRef)
                        .absenceDate(date)
                        .build());

        absenceMapper.applyUpdate(absence, type, isFinal, consecutiveDays);

        return absenceRepository.save(absence);
    }
}