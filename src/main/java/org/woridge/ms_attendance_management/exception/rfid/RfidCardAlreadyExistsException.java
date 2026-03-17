package org.woridge.ms_attendance_management.exception.rfid;



public class RfidCardAlreadyExistsException extends RuntimeException {

    public RfidCardAlreadyExistsException(String uid) {
        super("Cette carte RFID est déjà enregistrée : " + uid);
    }
}