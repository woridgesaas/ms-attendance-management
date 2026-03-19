package org.woridge.ms_attendance_management.dto;

import org.woridge.ms_attendance_management.enumeration.ErrorCode;

import java.time.Instant;

public record ErrorResponse<T>(
        Instant timestamp,
        T message,
        ErrorCode errorCode
) {
    public static <T> ErrorResponse<T> createErrorResponse(T message, ErrorCode errorCode) {
        return new ErrorResponse<>(Instant.now(), message, errorCode);
    }
}