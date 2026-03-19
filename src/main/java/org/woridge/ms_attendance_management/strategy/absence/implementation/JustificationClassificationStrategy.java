package org.woridge.ms_attendance_management.strategy.absence.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.repository.JustificationRepository;
import org.woridge.ms_attendance_management.strategy.absence.AbsenceClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JustificationClassificationStrategy implements AbsenceClassificationStrategy {

    private final JustificationRepository justificationRepository;

    @Override
    public boolean canHandle(UUID userId, LocalDate date) {
        return justificationRepository
                .findJustificationStatusForUserAndDate(userId, date)
                .isPresent();
    }

    @Override
    public AbsenceType classify(UUID userId, LocalDate date) {
        String status = justificationRepository
                .findJustificationStatusForUserAndDate(userId, date)
                .orElse(null);

        return switch (status) {
            case "APPROVED" -> AbsenceType.JUSTIFIED_JUSTIFICATION;
            case "PENDING"  -> AbsenceType.PENDING_JUSTIFICATION;
            default         -> AbsenceType.UNJUSTIFIED;
        };
    }
}
