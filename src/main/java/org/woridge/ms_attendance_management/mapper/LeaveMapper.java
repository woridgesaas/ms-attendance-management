package org.woridge.ms_attendance_management.mapper;

import org.woridge.ms_attendance_management.dto.leave.ApproveLeaveResponse;
import org.woridge.ms_attendance_management.dto.leave.ApprovedBy;
import org.woridge.ms_attendance_management.messaging.event.ApproveLeaveEvent;
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

    public static ApproveLeaveEvent toPublishEvent(LeaveRequest model , Integer remainingSold){
        return  ApproveLeaveEvent
                .builder()
                    .employeeCin(model.getUser().getCin())
                    .employeeEmail(model.getUser().getEmail())
                    .employeeFullName(model.getUser().getFullName())

                    .leaveType(model.getLeaveType())
                    .startDate(model.getStartDate())
                    .endDate(model.getEndDate())

                    .decisionStatus(model.getValidation().getDecision())
                    .decisionDate(model.getValidation().getDecisionDate())
                    .approvedByFullName(model.getValidation().getPerformedBy().getFullName())

                .remainingBalance(remainingSold)
                .build();
    }
}
