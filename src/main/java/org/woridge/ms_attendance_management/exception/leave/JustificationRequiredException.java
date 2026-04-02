package org.woridge.ms_attendance_management.exception.leave;

public class JustificationRequiredException extends RuntimeException {
    public JustificationRequiredException(String message) {
        super(message);
    }
}
