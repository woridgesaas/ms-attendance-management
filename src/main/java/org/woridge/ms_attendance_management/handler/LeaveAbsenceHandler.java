package org.woridge.ms_attendance_management.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.strategy.absence.implementation.LeaveClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveAbsenceHandler extends AbsenceHandler {

    private final LeaveClassificationStrategy strategy;

    @Override
    public AbsenceType handle(UUID userId, LocalDate date) {
        if (strategy.canHandle(userId, date)) {
            return strategy.classify(userId, date);
        }
        return passToNext(userId, date);
    }
}