package org.woridge.ms_attendance_management.messaging.event;

import lombok.Builder;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Builder

public record OvertimeHandleEvent(
        String employeeFullName,
        String employeeCin,
        String employeeEmail,
        OvertimeStatus status,
        String managerComment,
        Double totalHours,
        LocalDate workingDay,
        LocalDateTime processedAt
)
{}
