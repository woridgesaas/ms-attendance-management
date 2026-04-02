package org.woridge.ms_attendance_management.enumeration;

public enum LeaveType {
    ANNUEL,
    MALADIE,
    EXCEPTIONNEL;

    public boolean requiresJustification() {
        return this == MALADIE || this == EXCEPTIONNEL;
    }
}