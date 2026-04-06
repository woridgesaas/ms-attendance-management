package org.woridge.ms_attendance_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.woridge.ms_attendance_management.dto.SuccessResponse;
import org.woridge.ms_attendance_management.dto.leave.ApproveLeaveResponse;
import org.woridge.ms_attendance_management.mapper.LeaveMapper;
import org.woridge.ms_attendance_management.service.LeaveService;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/leaves")
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping("/{id}/approvals")
    public ResponseEntity<SuccessResponse<ApproveLeaveResponse>> approve(HttpServletRequest request ,@PathVariable("id") UUID requestLeaveId){
        String managerCin = request.getHeader("X-USER-CIN");
        var leaveRequest=this.leaveService.approveLeave(requestLeaveId , managerCin);
       return  ResponseEntity.status(HttpStatus.CREATED)
               .body(SuccessResponse.createSuccessResponse(
                       "Leave approves",
                       LeaveMapper.toApprovalsLeaveResponse(leaveRequest)));

    }
}
