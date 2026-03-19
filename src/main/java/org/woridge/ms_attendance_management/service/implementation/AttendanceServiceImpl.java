package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.dto.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.AttendanceResponse;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;
import org.woridge.ms_attendance_management.exception.AttendanceNotFoundException;
import org.woridge.ms_attendance_management.mapper.AttendanceMapper;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.repository.WorkingDayRepository;
import org.woridge.ms_attendance_management.service.AttendanceService;
import org.woridge.ms_attendance_management.utils.AttendanceCalculator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.woridge.ms_attendance_management.utils.AttendanceCalculator.calculateWorkedHours;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final WorkingDayRepository repository;
    private final AttendanceMapper mapper;

    @Override
    public List<AttendanceResponse> getUserAttendance(UUID userId) {

        return repository.findByPerformedBy(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public double calculateWeekHours(UUID userId) {

        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = LocalDate.now().with(DayOfWeek.SUNDAY);

        return repository.findWeek(userId, start, end)
                .stream()
                .mapToDouble(this::calculateHours)
                .sum();
    }

    @Override
    public double calculateMonthHours(UUID userId) {

        YearMonth month = YearMonth.now();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return repository.findWeek(userId, start, end)
                .stream()
                .mapToDouble(this::calculateHours)
                .sum();
    }

    @Override
    public AttendanceChartResponse getTodayChart(UUID userId) {

        WorkingDay day = getTodayAttendance(userId);

        double worked = calculateHours(day);

        double productive = AttendanceCalculator.calculateProductiveHours(day.getDate(), worked);

        double overtime = AttendanceCalculator.calculateOvertime(day.getDate(), worked);

        return mapper.toChartResponse(worked, productive, overtime);
    }

    @Override
    public double calculateTodayHours(UUID userId) {

        WorkingDay day = getTodayAttendance(userId);

        return calculateHours(day);
    }

    private WorkingDay getTodayAttendance(UUID userId) {

        return repository.findByPerformedByAndDate(userId, LocalDate.now())
                .orElseThrow(() ->
                        new AttendanceNotFoundException("No attendance found for today"));
    }

    private double calculateHours(WorkingDay day) {

        return AttendanceCalculator.calculateWorkedHours(
                day.getCheckInTime(),
                day.getCheckOutTime()
        );
    }

}