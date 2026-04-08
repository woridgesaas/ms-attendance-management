package org.woridge.ms_attendance_management.messaging.event;

import lombok.Builder;
import lombok.Data;
import org.woridge.ms_attendance_management.enumeration.LeaveRequestDecision;
import org.woridge.ms_attendance_management.enumeration.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder

/**
 * Event data for leave request approval notification.
 */
public record ApproveLeaveEvent(
        String employeeCin,
        String employeeEmail,

        String employeeFullName,

        LocalDate startDate,
        LocalDate endDate,
        LeaveType leaveType,

        LeaveRequestDecision decisionStatus,
        LocalDateTime decisionDate,
        String approvedByFullName,

        Integer remainingBalance
) {
}
