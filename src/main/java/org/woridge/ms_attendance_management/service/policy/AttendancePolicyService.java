package org.woridge.ms_attendance_management.service.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;
import org.woridge.ms_attendance_management.repository.HolidayRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Contains all attendance business rules.
 */
@Service
@RequiredArgsConstructor
public class AttendancePolicyService {

    private final HolidayRepository holidayRepository;


    private static final double STANDARD_WORKING_HOURS = 8.0;
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(17, 0);

    public double calculateWorkedHours(LocalTime in, LocalTime out) {

        if (in == null) return 0;

        if (out == null)
            return Duration.between(in, LocalTime.now()).toMinutes() / 60.0;

        if (out.isBefore(in)) return 0;

        return Duration.between(in, out).toMinutes() / 60.0;
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6;
    }

    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
    public double calculateProductiveHours(LocalDate date, double worked) {

        if (isWeekend(date) || isHoliday(date)) return 0;

        return Math.min(worked, STANDARD_WORKING_HOURS);
    }

    public double calculateOvertime(LocalDate date, double worked) {

        if (isWeekend(date) && worked > 0)
            return worked;
        if (worked > STANDARD_WORKING_HOURS)
            return worked - STANDARD_WORKING_HOURS;

        return 0;
    }

    public AttendanceStatus calculateStatus(LocalDate date, LocalTime in, LocalTime out) {

        if (in == null) return AttendanceStatus.ABSENT;

        if (isHoliday(date)) return AttendanceStatus.HOLIDAY;

        if (isWeekend(date))
            return AttendanceStatus.WEEKEND;

        double worked = calculateWorkedHours(in, out);

        if (in.isAfter(START)) return AttendanceStatus.LATE;

        if (out != null && out.isBefore(END))
            return AttendanceStatus.EARLY_LEAVE;

        if (worked < 4) return AttendanceStatus.UNDER_TIME;

        if (worked < STANDARD_WORKING_HOURS)
            return AttendanceStatus.HALF_DAY;

        if (worked > STANDARD_WORKING_HOURS)
            return AttendanceStatus.OVERTIME;

        return AttendanceStatus.PRESENT;
    }
}