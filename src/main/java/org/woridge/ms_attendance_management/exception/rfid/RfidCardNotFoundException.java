package org.woridge.ms_attendance_management.exception.rfid;


public class RfidCardNotFoundException extends RuntimeException {

    public RfidCardNotFoundException(String uid) {
        super("Carte RFID introuvable pour l'UID : " + uid);
    }
}