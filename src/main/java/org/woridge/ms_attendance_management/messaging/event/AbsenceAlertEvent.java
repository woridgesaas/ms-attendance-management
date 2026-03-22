package org.woridge.ms_attendance_management.messaging.event;

import lombok.Builder;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;

import java.util.List;

@Builder
public record AbsenceAlertEvent(
        String employeeFullName,
        String employeeEmail,
        String absenceDate,
        int consecutiveDays,
        AlertLevel alertLevel,
        List<String> recipientCins
) {}
