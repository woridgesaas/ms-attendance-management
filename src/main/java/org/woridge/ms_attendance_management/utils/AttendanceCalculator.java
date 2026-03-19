package org.woridge.ms_attendance_management.utils;

import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public final class AttendanceCalculator {

    private static final double STANDARD_WORKING_HOURS = 8.0;

    private AttendanceCalculator() {}

    public static double calculateWorkedHours(LocalTime checkIn, LocalTime checkOut) {

        if (checkIn == null || checkOut == null)
            return 0;

        Duration duration = Duration.between(checkIn, checkOut);

        return duration.toMinutes() / 60.0;
    }

    public static boolean isWeekend(LocalDate date) {

        return date.getDayOfWeek().getValue() >= 6;
    }

    public static double calculateProductiveHours(LocalDate date, double workedHours) {

        if (isWeekend(date))
            return 0;

        return Math.min(workedHours, STANDARD_WORKING_HOURS);
    }

    public static double calculateOvertime(LocalDate date, double workedHours) {

        if (isWeekend(date))
            return workedHours;

        if (workedHours > STANDARD_WORKING_HOURS)
            return workedHours - STANDARD_WORKING_HOURS;

        return 0;
    }

    public static AttendanceStatus calculateStatus(
            LocalDate date,
            LocalTime checkIn,
            LocalTime checkOut
    ) {

        if(checkIn == null)
            return AttendanceStatus.ABSENT;

        double workedHours = calculateWorkedHours(checkIn, checkOut);

        if (isWeekend(date))
            return AttendanceStatus.OVERTIME;

        // TODO : check holidays from Holiday microservice

        if (workedHours > STANDARD_WORKING_HOURS)
            return AttendanceStatus.OVERTIME;

        if (workedHours == STANDARD_WORKING_HOURS)
            return AttendanceStatus.PRESENT;

        return AttendanceStatus.WARNING;
    }

}