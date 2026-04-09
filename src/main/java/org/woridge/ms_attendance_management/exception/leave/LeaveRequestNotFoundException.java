package org.woridge.ms_attendance_management.exception.leave;

public class LeaveRequestNotFoundException extends RuntimeException {
    public LeaveRequestNotFoundException(String message) {
        super(message);
    }
}
