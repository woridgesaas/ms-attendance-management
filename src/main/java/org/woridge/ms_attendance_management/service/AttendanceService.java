package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.dto.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.AttendanceResponse;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    List<AttendanceResponse> getUserAttendance(UUID userId);

    double calculateWeekHours(UUID userId);

    double calculateMonthHours(UUID userId);

    AttendanceChartResponse getTodayChart(UUID userId);

    double calculateTodayHours(UUID userId);


}