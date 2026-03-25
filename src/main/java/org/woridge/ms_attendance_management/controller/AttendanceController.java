package org.woridge.ms_attendance_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woridge.ms_attendance_management.dto.SuccessResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceDashboardResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceResponse;
import org.woridge.ms_attendance_management.service.AttendanceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<AttendanceResponse>>> getAttendance(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Attendance retrieved successfully",
                        attendanceService.getUserAttendance(userId)
                )
        );
    }

    @GetMapping("/today")
    public ResponseEntity<SuccessResponse<Double>> getTodayHours(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Today's hours retrieved successfully",
                        attendanceService.calculateTodayHours(userId)
                )
        );
    }

    @GetMapping("/week")
    public ResponseEntity<SuccessResponse<Double>> getWeekHours(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Weekly hours retrieved successfully",
                        attendanceService.calculateWeekHours(userId)
                )
        );
    }

    @GetMapping("/month")
    public ResponseEntity<SuccessResponse<Double>> getMonthHours(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Monthly hours retrieved successfully",
                        attendanceService.calculateMonthHours(userId)
                )
        );
    }

    @GetMapping("/chart/today")
    public ResponseEntity<SuccessResponse<AttendanceChartResponse>> getTodayChart(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Today's chart retrieved successfully",
                        attendanceService.getTodayChart(userId)
                )
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<AttendanceDashboardResponse>> getDashboard(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Dashboard retrieved successfully",
                        attendanceService.getDashboard(userId)
                )
        );
    }
}