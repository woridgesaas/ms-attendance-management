package org.woridge.ms_attendance_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.woridge.ms_attendance_management.dto.SuccessResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceListResponse;
import org.woridge.ms_attendance_management.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
public class AttendanceListController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<AttendanceListResponse>>> getAttendanceList(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID departmentId
    ) {
        String userCin = request.getHeader("X-User-CIN");
        String role = request.getHeader("X-User-Role");

        List<AttendanceListResponse> response = attendanceService.getAttendanceList(
                userCin, role, startDate, endDate, departmentId
        );

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Attendance list retrieved successfully",
                        response
                )
        );
    }
}