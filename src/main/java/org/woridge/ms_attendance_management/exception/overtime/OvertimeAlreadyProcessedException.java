package org.woridge.ms_attendance_management.exception.overtime;

public class OvertimeAlreadyProcessedException extends RuntimeException {
    public OvertimeAlreadyProcessedException(String message) {
        super(message);
    }
}
