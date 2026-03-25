package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceResponse;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;
import org.woridge.ms_attendance_management.exception.AttendanceNotFoundException;
import org.woridge.ms_attendance_management.mapper.AttendanceMapper;
import org.woridge.ms_attendance_management.model.WorkingDay;
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

    private final WorkingDayRepository repository;
    private final AttendanceMapper mapper;
    private final AttendancePolicyService policy;

    /**
     * Get all attendance records for a user.
     */
    @Override
    public List<AttendanceResponse> getUserAttendance(UUID userId) {

        return repository.findByPerformedBy(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Calculate total worked hours for current week.
     */
    @Override
    public double calculateWeekHours(UUID userId) {

        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = LocalDate.now().with(DayOfWeek.SUNDAY);

        return repository.findWeek(userId, start, end)
                .stream()
                .mapToDouble(this::calculateWorkedHours)
                .sum();
    }

    /**
     * Calculate total worked hours for current month.
     */
    @Override
    public double calculateMonthHours(UUID userId) {

        YearMonth month = YearMonth.now();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return repository.findWeek(userId, start, end)
                .stream()
                .mapToDouble(this::calculateWorkedHours)
                .sum();
    }

    /**
     * Get today's chart data (total, productive, overtime).
     */
    @Override
    public AttendanceChartResponse getTodayChart(UUID userId) {

        WorkingDay day = getTodayAttendance(userId);

        double worked = calculateWorkedHours(day);
        double productive = policy.calculateProductiveHours(day.getDate(), worked);
        double overtime = policy.calculateOvertime(day.getDate(), worked);

        return mapper.toChartResponse(worked, productive, overtime);
    }

    /**
     * Get today's worked hours.
     */
    @Override
    public double calculateTodayHours(UUID userId) {

        WorkingDay day = getTodayAttendance(userId);

        return calculateWorkedHours(day);
    }

    /**
     * Retrieve today's attendance or throw exception.
     */
    private WorkingDay getTodayAttendance(UUID userId) {

        return repository.findByPerformedByAndDate(userId, LocalDate.now())
                .orElseThrow(() ->
                        new AttendanceNotFoundException("No attendance found for today"));
    }

    /**
     * Map entity to response using business logic.
     */
    private AttendanceResponse mapToResponse(WorkingDay day) {

        double worked = calculateWorkedHours(day);

        AttendanceStatus status = policy.calculateStatus(
                day.getDate(),
                day.getCheckInTime(),
                day.getCheckOutTime()
        );

        return mapper.toResponse(day, status, worked);
    }

    /**
     * Calculate worked hours using policy.
     */
    private double calculateWorkedHours(WorkingDay day) {

        return policy.calculateWorkedHours(
                day.getCheckInTime(),
                day.getCheckOutTime()
        );
    }
}