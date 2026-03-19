package org.woridge.ms_attendance_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Entry time is required")
    private LocalTime entry;

    @NotNull(message = "Exit time is required")
    private LocalTime exit;

    @NotNull(message = "Attendance status is required")
    private String status;

    @PositiveOrZero(message = "Production hours must be positive")
    private double productionHours;
}