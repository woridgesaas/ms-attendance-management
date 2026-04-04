package org.woridge.ms_attendance_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.woridge.ms_attendance_management.dto.SuccessResponse;
import org.woridge.ms_attendance_management.dto.overtime.ApproveOvertimeRequest;
import org.woridge.ms_attendance_management.dto.overtime.OvertimeIndexResponse;
import org.woridge.ms_attendance_management.dto.overtime.RefuseOvertimeRequest;
import org.woridge.ms_attendance_management.mapper.OvertimeMapper;
import org.woridge.ms_attendance_management.service.OvertimeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/overtimes")
@RequiredArgsConstructor
public class OvertimeController {
    @Autowired
    private OvertimeService overtimeService;

    @GetMapping("")
    public ResponseEntity<SuccessResponse<List<OvertimeIndexResponse>>> getAllByDepartment(HttpServletRequest request){
        String cin = request.getHeader("X-User-CIN");
        var overtimes = this.overtimeService.getPendingByDepartment(cin);
        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse("Data fetched" ,
                        overtimes.stream().map(OvertimeMapper::toIndexResponse).toList()
                ));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable UUID id,
            HttpServletRequest request) {

        String managerCin = request.getHeader("X-User-CIN");

        overtimeService.approveOvertime(id, managerCin);

        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/reject")
    public  ResponseEntity refuse(@PathVariable UUID id, @Valid @RequestBody RefuseOvertimeRequest dto ,HttpServletRequest request){
        String managerCin = request.getHeader("X-User-CIN");
        overtimeService.refuseOvertime(id , managerCin , dto.raison());
        return ResponseEntity.noContent().build();
    }
}
