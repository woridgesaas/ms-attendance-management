package org.woridge.ms_attendance_management.exception.rfid;


import java.util.UUID;

public class EmployeeAlreadyHasCardException extends RuntimeException {

    public EmployeeAlreadyHasCardException(UUID employeeId) {
        super("Cet employé possède déjà une carte RFID : " + employeeId);
    }
}