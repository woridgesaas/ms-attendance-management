package org.woridge.ms_attendance_management.exception.overtime;

public class OvertimeNotFoundException extends RuntimeException {
    public OvertimeNotFoundException(String message) {
        super(message);
    }
}
