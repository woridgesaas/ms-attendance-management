package org.woridge.ms_attendance_management.strategy.absence.implementation;


import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.strategy.absence.AbsenceClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class DefaultClassificationStrategy implements AbsenceClassificationStrategy {

    @Override
    public boolean canHandle(UUID userId, LocalDate date) {
        return true; // toujours applicable — fin de chaîne
    }

    @Override
    public AbsenceType classify(UUID userId, LocalDate date) {
        return AbsenceType.UNJUSTIFIED;
    }
}
