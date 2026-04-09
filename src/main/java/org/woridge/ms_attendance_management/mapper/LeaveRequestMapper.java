package org.woridge.ms_attendance_management.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.leave.*;
import org.woridge.ms_attendance_management.exception.leave.LeaveRequestException;
import org.woridge.ms_attendance_management.model.LeaveRequest;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.utils.DateUtils;

@Component
@AllArgsConstructor
public class LeaveRequestMapper {
    private final ObjectMapper objectMapper;

    public CreateLeaveRequest toCreateLeaveRequest(String data){
        try {
            return objectMapper.readValue(data, CreateLeaveRequest.class);
        } catch (JsonProcessingException e) {
            throw new LeaveRequestException("Format JSON invalide : " + e.getMessage());
        }
    }

    public LeaveRequest toLeaveRequest(CreateLeaveRequest dto, User user) {
        return LeaveRequest.builder()
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .leaveType(dto.getLeaveType())
                .user(user)
                .build();
    }

    public LeaveRequestResponse toLeaveRequestResponse(
            LeaveRequest leaveRequest,
            int numberOfDays,
            int remainingBalance,
            String justificationPath) {

        return LeaveRequestResponse.builder()
                .id(leaveRequest.getId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .numberOfDays(numberOfDays)
                .remainingBalance(remainingBalance)
                .justificationPath(justificationPath)
                .requestDate(leaveRequest.getRequestDate())
                .build();
    }
    public LeaveRequestListResponse toListResponse(LeaveRequest leaveRequest) {

        ApprovedBy approvedBy = null;
        if (leaveRequest.getValidation() != null
                && leaveRequest.getValidation().getPerformedBy() != null) {

            User validator = leaveRequest.getValidation().getPerformedBy();
            approvedBy = ApprovedBy.builder()
                    .fullName(validator.getFirstName() + " " + validator.getLastName())
                    .build();
        }

        return LeaveRequestListResponse.builder()
                .id(leaveRequest.getId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .numberOfDays(DateUtils.calculateBusinessDays(
                        leaveRequest.getStartDate(), leaveRequest.getEndDate()))
                .status(leaveRequest.getValidation() != null
                        ? leaveRequest.getValidation().getDecision().name()
                        : "NEW")
                .approvedBy(approvedBy)
                .build();
    }
    public LeaveRequestAdminResponse toAdminResponse(LeaveRequest leaveRequest, String role) {

        User user = leaveRequest.getUser();

        String employeeName = user.getFullName();

        String approvedBy = null;

        if (leaveRequest.getValidation() != null &&
                leaveRequest.getValidation().getPerformedBy() != null) {

            User manager = leaveRequest.getValidation().getPerformedBy();
            approvedBy = manager.getFullName();
        }

        int numberOfDays = DateUtils.calculateBusinessDays(
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        );

        return LeaveRequestAdminResponse.builder()
                .id(leaveRequest.getId())
                .employeeName(employeeName)
                .employeeRole("EMPLOYEE")
                .leaveType(leaveRequest.getLeaveType().name())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .numberOfDays(numberOfDays)
                .status(
                        leaveRequest.getValidation() != null
                                ? leaveRequest.getValidation().getDecision().name()
                                : "PENDING"
                )
                .approvedBy(approvedBy)
                .build();
    }
}