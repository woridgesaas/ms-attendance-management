package org.woridge.ms_attendance_management.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.woridge.ms_attendance_management.enumeration.LeaveRequestDecision;
import org.woridge.ms_attendance_management.model.User;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveLeaveResponse {
    UUID leaveRequestId;
    String firstName;
    String lastName;
    LeaveRequestDecision status;
    ApprovedBy validateBy;
}
