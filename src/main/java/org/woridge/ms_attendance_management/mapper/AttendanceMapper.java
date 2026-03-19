package org.woridge.ms_attendance_management.mapper;

import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.AttendanceChartResponse;
import org.woridge.ms_attendance_management.dto.AttendanceResponse;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.utils.AttendanceCalculator;

@Component
public class AttendanceMapper {

    public AttendanceResponse toResponse(WorkingDay entity) {

        double hours = AttendanceCalculator.calculateWorkedHours(
                entity.getCheckInTime(),
                entity.getCheckOutTime()
        );

        return AttendanceResponse.builder()
                .date(entity.getDate())
                .entry(entity.getCheckInTime())
                .exit(entity.getCheckOutTime())
                .status(
                        AttendanceCalculator.calculateStatus(
                                entity.getDate(),
                                entity.getCheckInTime(),
                                entity.getCheckOutTime()
                        ).name()
                )
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