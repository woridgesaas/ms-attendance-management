package org.woridge.ms_attendance_management.dto.absence;

import org.woridge.ms_attendance_management.enumeration.AlertLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record AbsenceAlertResponse(

        UUID id,
        UUID userId,
        AlertLevel alertLevel,
        int consecutiveDays,
        LocalDateTime triggeredAt
) {}
