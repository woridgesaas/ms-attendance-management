package org.woridge.ms_attendance_management.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.strategy.absence.implementation.DefaultClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultAbsenceHandler extends AbsenceHandler {

    private final DefaultClassificationStrategy strategy;

    @Override
    public AbsenceType handle(UUID userId, LocalDate date) {
        return strategy.classify(userId, date);
    }
}
