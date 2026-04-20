package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceDashboardResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceListResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    List<AttendanceResponse> getUserAttendance(UUID userId);

    double calculateWeekHours(UUID userId);

    double calculateMonthHours(UUID userId);

    AttendanceChartResponse getTodayChart(UUID userId);

    double calculateTodayHours(UUID userId);

    AttendanceDashboardResponse getDashboard(UUID userId);
    List<AttendanceListResponse> getAttendanceList(
            String userCin, String role,
            LocalDate startDate, LocalDate endDate,
            UUID departmentId);



}