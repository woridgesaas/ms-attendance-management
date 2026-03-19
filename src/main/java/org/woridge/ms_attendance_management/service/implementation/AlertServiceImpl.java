package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;
import org.woridge.ms_attendance_management.enumeration.AlertResult;
import org.woridge.ms_attendance_management.model.AbsenceAlert;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.propreties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.AbsenceAlertRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.AlertService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AbsenceAlertRepository absenceAlertRepository;
    private final BusinessProperties     businessProperties;
    private final UserRepository         userRepository;

    // Phase RabbitMQ — publisher sera injecté ici

    @Override
    public AlertResult evaluate(UUID userId, int consecutiveDays) {

        if (consecutiveDays >= businessProperties.getCeoAlertThreshold()) {
            return triggerIfNotDuplicate(userId, AlertLevel.CEO, consecutiveDays);

        } else if (consecutiveDays >= businessProperties.getRhAlertThreshold()) {
            return triggerIfNotDuplicate(userId, AlertLevel.RH, consecutiveDays);
        }
        return AlertResult.NO_THRESHOLD;

    }

    private AlertResult triggerIfNotDuplicate(UUID userId, AlertLevel level, int consecutiveDays) {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        boolean alreadyTriggered = absenceAlertRepository
                .existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                        userId, level, startOfDay, endOfDay
                );

        if (alreadyTriggered) {
            return AlertResult.SKIPPED_ALREADY_TRIGGERED;
        }

        // ✅ corrigé → proxy léger sans requête SQL supplémentaire
        User userRef = userRepository.getReferenceById(userId);

        AbsenceAlert alert = AbsenceAlert.builder()
                .user(userRef)
                .alertLevel(level)
                .consecutiveDays(consecutiveDays)
                .triggeredAt(LocalDateTime.now())
                .build();

        absenceAlertRepository.save(alert);


        // Phase RabbitMQ :
        // alertPublisher.publish(userId, level, consecutiveDays);
        return AlertResult.TRIGGERED;
    }
}