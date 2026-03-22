package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;
import org.woridge.ms_attendance_management.enumeration.AlertResult;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.messaging.event.AbsenceAlertEvent;
import org.woridge.ms_attendance_management.messaging.publisher.AbsenceAlertPublisher;
import org.woridge.ms_attendance_management.model.AbsenceAlert;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.properties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.AbsenceAlertRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.AlertService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AbsenceAlertRepository absenceAlertRepository;
    private final BusinessProperties     businessProperties;
    private final UserRepository         userRepository;
    private final AbsenceAlertPublisher absenceAlertPublisher;

    @Override
    public AlertResult evaluate(UUID userId, int consecutiveDays) {

        if (consecutiveDays >= businessProperties.getCeoAlertThreshold()) {
            return triggerIfNotDuplicate(userId, AlertLevel.CEO, consecutiveDays);
        } else if (consecutiveDays >= businessProperties.getRhAlertThreshold()) {
            return triggerIfNotDuplicate(userId, AlertLevel.RH, consecutiveDays);
        } else {
            return AlertResult.NO_THRESHOLD;
        }
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

        User userRef = userRepository.getReferenceById(userId);

        AbsenceAlert alert = AbsenceAlert.builder()
                .user(userRef)
                .alertLevel(level)
                .consecutiveDays(consecutiveDays)
                .triggeredAt(LocalDateTime.now())
                .build();

        absenceAlertRepository.save(alert);

        publishAlert(userId, level, consecutiveDays);
        return AlertResult.TRIGGERED;
    }

    private void publishAlert(UUID userId, AlertLevel level, int consecutiveDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId
                ));

        AbsenceAlertEvent event = AbsenceAlertEvent.builder()
                .employeeFullName(user.getFirstName() + " " + user.getLastName())
                .employeeEmail(user.getEmail())
                .absenceDate(LocalDate.now().toString())
                .consecutiveDays(consecutiveDays)
                .alertLevel(level)
                .recipientCins(List.of("AB1234567")) // futur → gateway
                .build();

        absenceAlertPublisher.publish(event);
    }
    }

