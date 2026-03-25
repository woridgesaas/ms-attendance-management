package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceDashboardResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.HoursWithLimit;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;
import org.woridge.ms_attendance_management.exception.AttendanceNotFoundException;
import org.woridge.ms_attendance_management.mapper.AttendanceMapper;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.properties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.WorkingDayRepository;
import org.woridge.ms_attendance_management.service.AttendanceService;
import org.woridge.ms_attendance_management.service.policy.AttendancePolicyService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for orchestrating attendance operations.
 *
 * <p>This layer coordinates:
 * - repository access
 * - business rules (via AttendancePolicyService)
 * - mapping to DTOs</p>
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final WorkingDayRepository WorkingDayRepository;
    private final AttendanceMapper AttendanceMapper;
    private final AttendancePolicyService AttendancePolicyService;
    private final BusinessProperties businessProperties;


    // ===============================
    // PUBLIC METHODS
    // ===============================

    @Override
    public List<AttendanceResponse> getUserAttendance(UUID userId) {
        return WorkingDayRepository.findByPerformedBy_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public double calculateWeekHours(UUID userId) {
        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = LocalDate.now().with(DayOfWeek.SUNDAY);

        return calculatePeriod(userId, start, end);
    }

    @Override
    public double calculateMonthHours(UUID userId) {
        YearMonth month = YearMonth.now();

        return calculatePeriod(
                userId,
                month.atDay(1),
                month.atEndOfMonth()
        );
    }

    @Override
    public double calculateTodayHours(UUID userId) {
        WorkingDay day = getTodayAttendance(userId);
        return workedHours(day);
    }

    @Override
    public AttendanceChartResponse getTodayChart(UUID userId) {

        WorkingDay day = getTodayAttendance(userId);

        double worked = workedHours(day);
        double productive = AttendancePolicyService.calculateProductiveHours(day.getDate(), worked);
        double overtime = AttendancePolicyService.calculateOvertime(day.getDate(), worked);

        return AttendanceMapper.toChartResponse(worked, productive, overtime);
    }

    @Override
    public AttendanceDashboardResponse getDashboard(UUID userId) {

        double today = calculateTodayHours(userId);
        double week = calculateWeekHours(userId);
        double month = calculateMonthHours(userId);

        double overtime = WorkingDayRepository.findByPerformedBy_Id(userId)
                .stream()
                .mapToDouble(day -> AttendancePolicyService.calculateOvertime(
                        day.getDate(),
                        workedHours(day)
                ))
                .sum();

        return AttendanceDashboardResponse.builder()
                .today(new HoursWithLimit(today, businessProperties.getDailyMaxHours()))
                .week(new HoursWithLimit(week, businessProperties.getWeeklyMaxHours()))
                .month(new HoursWithLimit(month, businessProperties.getMonthlyMaxHours()))
                .overtimeHours(new HoursWithLimit(overtime, businessProperties.getOvertimeMaxHours()))
                .build();
    }

    // ===============================
    // PRIVATE HELPERS (CLEAN CODE)
    // ===============================

    private double calculatePeriod(UUID userId, LocalDate start, LocalDate end) {
        return WorkingDayRepository.findBetweenDates(userId, start, end)
                .stream()
                .mapToDouble(this::workedHours)
                .sum();
    }

    private WorkingDay getTodayAttendance(UUID userId) {
        return WorkingDayRepository.findByPerformedBy_IdAndDate(userId, LocalDate.now())
                .orElseThrow(() ->
                        new AttendanceNotFoundException("No attendance found for today"));
    }

    private AttendanceResponse mapToResponse(WorkingDay day) {

        double worked = workedHours(day);

        return AttendanceMapper.toResponse(
                day,
                AttendancePolicyService.calculateStatus(
                        day.getDate(),
                        day.getCheckInTime(),
                        day.getCheckOutTime()
                ),
                worked
        );
    }

    private double workedHours(WorkingDay day) {
        return AttendancePolicyService.calculateWorkedHours(
                day.getCheckInTime(),
                day.getCheckOutTime()
        );
    }
}