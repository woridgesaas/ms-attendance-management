package org.woridge.ms_attendance_management.enumeration;

public enum AbsenceType {

    JUSTIFIED_LEAVE,
    JUSTIFIED_JUSTIFICATION,
    PENDING_JUSTIFICATION,
    UNJUSTIFIED,
    UNJUSTIFIED_FINAL;

    public boolean isJustified() {
        return this == JUSTIFIED_LEAVE || this == JUSTIFIED_JUSTIFICATION;
    }

    public boolean isFinal() {
        return this == UNJUSTIFIED_FINAL;
    }

    public boolean isUnjustified() {
        return this == UNJUSTIFIED || this == PENDING_JUSTIFICATION;
    }
}