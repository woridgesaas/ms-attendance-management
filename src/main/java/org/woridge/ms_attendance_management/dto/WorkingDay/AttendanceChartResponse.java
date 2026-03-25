package org.woridge.ms_attendance_management.dto.WorkingDay;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceChartResponse {

    @PositiveOrZero(message = "Total hours must be positive")
    private double totalHours;

    @PositiveOrZero(message = "Productive hours must be positive")
    private double productiveHours;

    @PositiveOrZero(message = "Overtime hours must be positive")
    private double overtimeHours;

}