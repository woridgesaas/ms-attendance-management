package org.woridge.ms_attendance_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceResponse;
import org.woridge.ms_attendance_management.service.AttendanceService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for attendance operations.
 */
@RestController
@RequestMapping("/users/{userId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Get all attendance records of a user.
     */
    @GetMapping
    public List<AttendanceResponse> getAttendance(@PathVariable UUID userId) {
        return attendanceService.getUserAttendance(userId);
    }

    /**
     * Get today's worked hours.
     */
    @GetMapping("/today")
    public double getTodayHours(@PathVariable UUID userId) {
        return attendanceService.calculateTodayHours(userId);
    }

    /**
     * Get weekly worked hours.
     */
    @GetMapping("/week")
    public double getWeekHours(@PathVariable UUID userId) {
        return attendanceService.calculateWeekHours(userId);
    }

    /**
     * Get monthly worked hours.
     */
    @GetMapping("/month")
    public double getMonthHours(@PathVariable UUID userId) {
        return attendanceService.calculateMonthHours(userId);
    }

    /**
     * Get today's chart (total / productive / overtime).
     */
    @GetMapping("/chart/today")
    public AttendanceChartResponse getTodayChart(@PathVariable UUID userId) {
        return attendanceService.getTodayChart(userId);
    }
}