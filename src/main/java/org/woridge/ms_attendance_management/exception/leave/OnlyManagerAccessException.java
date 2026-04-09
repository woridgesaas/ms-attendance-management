package org.woridge.ms_attendance_management.exception.leave;

public class OnlyManagerAccessException extends RuntimeException {
    public OnlyManagerAccessException(String message) {
        super(message);
    }
}
