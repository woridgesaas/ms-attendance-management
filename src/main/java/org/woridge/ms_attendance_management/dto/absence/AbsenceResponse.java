package org.woridge.ms_attendance_management.dto.absence;

import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AbsenceResponse(

        UUID id,
        UUID userId,
        LocalDate absenceDate,
        AbsenceType absenceType,
        boolean isFinal,
        int consecutiveDays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
