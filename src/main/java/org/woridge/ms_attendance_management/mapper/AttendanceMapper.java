package org.woridge.ms_attendance_management.mapper;

import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.WorkingDay.AttendanceResponse;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;
import org.woridge.ms_attendance_management.model.WorkingDay;

@Component
public class AttendanceMapper {

    public AttendanceResponse toResponse(
            WorkingDay entity,
            AttendanceStatus status,
            double hours
    ) {
        return AttendanceResponse.builder()
                .date(entity.getDate())
                .entry(entity.getCheckInTime())
                .exit(entity.getCheckOutTime())
                .status(status)
                .productionHours(hours)
                .build();
    }

    public AttendanceChartResponse toChartResponse(
            double total,
            double productive,
            double overtime
    ) {
        return AttendanceChartResponse.builder()
                .totalHours(total)
                .productiveHours(productive)
                .overtimeHours(overtime)
                .build();
    }

}