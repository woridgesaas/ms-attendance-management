package org.woridge.ms_attendance_management.dto;

import java.time.Instant;

public record SuccessResponse<T>(
        Instant timestamp,
        String message,
        T data
) {
    public static <T> SuccessResponse<T> createSuccessResponse(String message, T data) {
        return new SuccessResponse<>(Instant.now(), message, data);
    }
}
