package org.woridge.ms_attendance_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.woridge.ms_attendance_management.dto.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.AttendanceResponse;
import org.woridge.ms_attendance_management.service.AttendanceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/{userId}")
    public List<AttendanceResponse> getAttendance(@PathVariable UUID userId) {
        return attendanceService.getUserAttendance(userId);
    }
    @GetMapping("/week/{userId}")
    public double getWeekHours(@PathVariable UUID userId){
        return attendanceService.calculateWeekHours(userId);
    }
    @GetMapping("/month/{userId}")
    public double getMonthHours(@PathVariable UUID userId){
        return attendanceService.calculateMonthHours(userId);
    }
    @GetMapping("/chart/today/{userId}")
    public AttendanceChartResponse getTodayChart(@PathVariable UUID userId) {
        return attendanceService.getTodayChart(userId);
    }
    @GetMapping("/today/{userId}")
    public double getTodayHours(@PathVariable UUID userId){
        return attendanceService.calculateTodayHours(userId);
    }
}