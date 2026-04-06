package org.woridge.ms_attendance_management.mapper;

import org.woridge.ms_attendance_management.dto.leave.ApproveLeaveResponse;
import org.woridge.ms_attendance_management.dto.leave.ApprovedBy;
import org.woridge.ms_attendance_management.model.LeaveRequest;

public class LeaveMapper {
    public static ApproveLeaveResponse toApprovalsLeaveResponse(LeaveRequest model){
        new ApproveLeaveResponse();
        return ApproveLeaveResponse.
                builder()
                        .leaveRequestId(model.getId())
                        .firstName(model.getUser().getFirstName())
                        .lastName(model.getUser().getLastName())
                        .status(model.getValidation().getDecision())
                        .validateBy(new ApprovedBy(
                                model.getValidation().
                                        getPerformedBy().
                                        getFullName()
                        ))
                .build();
    }
}
