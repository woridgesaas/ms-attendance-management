package org.woridge.ms_attendance_management.strategy.absence.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.repository.LeaveRequestRepository;
import org.woridge.ms_attendance_management.strategy.absence.AbsenceClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveClassificationStrategy implements AbsenceClassificationStrategy {

    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public boolean canHandle(UUID userId, LocalDate date) {
        return leaveRequestRepository
                .existsApprovedLeaveForUserAndDate(userId, date);
    }

    @Override
    public AbsenceType classify(UUID userId, LocalDate date) {
        return AbsenceType.JUSTIFIED_LEAVE;
    }
}
