package org.woridge.ms_attendance_management.exception.rfid;

import java.util.UUID;

public class DayAlreadyCompleteException extends RuntimeException {
    public DayAlreadyCompleteException(UUID employeeId) {
        super("Journée déjà complète pour l'employé : " + employeeId);
    }
}