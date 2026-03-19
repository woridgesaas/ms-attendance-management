package org.woridge.ms_attendance_management.dto.absence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.UUID;

public record AbsenceProcessingRequest(

        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "date is required")
        @PastOrPresent(message = "date must not be in the future")
        LocalDate date,

        @NotNull(message = "workedHours is required")
        @PositiveOrZero(message = "workedHours must be zero or positive")
        Double workedHours
) {}