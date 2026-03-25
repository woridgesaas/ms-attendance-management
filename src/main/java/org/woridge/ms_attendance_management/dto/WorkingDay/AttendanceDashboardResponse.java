package org.woridge.ms_attendance_management.dto.WorkingDay;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDashboardResponse {

    @PositiveOrZero(message = "Today hours must be positive")
    private double todayHours;

    @PositiveOrZero(message = "Week hours must be positive")
    private double weekHours;

    @PositiveOrZero(message = "Month hours must be positive")
    private double monthHours;

    @PositiveOrZero(message = "Overtime hours must be positive")
    private double overtimeHours;
}