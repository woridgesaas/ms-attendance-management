package org.woridge.ms_attendance_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.woridge.ms_attendance_management.dto.SuccessResponse;
import org.woridge.ms_attendance_management.dto.leave.ApproveLeaveResponse;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestAdminResponse;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestListResponse;
import org.woridge.ms_attendance_management.mapper.LeaveMapper;
import org.woridge.ms_attendance_management.mapper.LeaveRequestMapper;
import org.woridge.ms_attendance_management.model.LeaveRequest;
import org.woridge.ms_attendance_management.service.LeaveService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/leaves")
public class LeaveController {
    private final LeaveService leaveService;
    private final LeaveRequestMapper leaveRequestMapper;

    @PostMapping("/{id}/approvals")
    public ResponseEntity<SuccessResponse<ApproveLeaveResponse>> approve(HttpServletRequest request ,@PathVariable("id") UUID requestLeaveId){
        String managerCin = request.getHeader("X-USER-CIN");
        var leaveRequest=this.leaveService.approveLeave(requestLeaveId , managerCin);
       return  ResponseEntity.status(HttpStatus.CREATED)
               .body(SuccessResponse.createSuccessResponse(
                       "Leave approves",
                       LeaveMapper.toApprovalsLeaveResponse(leaveRequest)));

    }
    private String getHeader(HttpServletRequest request, String key) {
        String value = request.getHeader(key);
        if (value == null) {
            value = request.getHeader(key.toLowerCase());
        }
        return value;
    }
    @GetMapping("/pending")
    public ResponseEntity<SuccessResponse<List<LeaveRequestAdminResponse>>> getPendingLeaves(
            HttpServletRequest request
    ) {

        String managerCin = getHeader(request, "X-User-CIN");
        String role = getHeader(request, "X-User-Role");

        List<LeaveRequestAdminResponse> response =
                leaveService.getPendingLeaves(managerCin, role);

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Pending leave requests retrieved successfully",
                        response
                )
        );
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<SuccessResponse<ApproveLeaveResponse>> rejectLeave(
            HttpServletRequest request,
            @PathVariable UUID id
    ) {

        String managerCin = request.getHeader("X-User-CIN");

        LeaveRequest leave = leaveService.rejectLeave(id, managerCin);

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Leave rejected successfully",
                        LeaveMapper.toApprovalsLeaveResponse(leave)
                )
        );
    }
}
