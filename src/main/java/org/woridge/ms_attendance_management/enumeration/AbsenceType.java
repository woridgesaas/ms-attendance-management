package org.woridge.ms_attendance_management.enumeration;

public enum AbsenceType {

    JUSTIFIED_LEAVE,
    JUSTIFIED_JUSTIFICATION,
    PENDING_JUSTIFICATION,
    UNJUSTIFIED,
    UNJUSTIFIED_FINAL;

    // Absence complètement justifiée → pas de comptage consécutif
    public boolean isJustified() {
        return this == JUSTIFIED_LEAVE
                || this == JUSTIFIED_JUSTIFICATION;
    }

    // Absence qui déclenche alertes + peut devenir FINAL
    // PENDING_JUSTIFICATION → en attente → on n'alerte pas encore
    public boolean isUnjustified() {
        return this == UNJUSTIFIED
                || this == UNJUSTIFIED_FINAL;
    }

    // Absence en attente de validation → ni justifiée ni unjustified
    public boolean isPending() {
        return this == PENDING_JUSTIFICATION;
    }
}