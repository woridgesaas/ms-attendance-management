package org.woridge.ms_attendance_management.exception.rfid;



public class ScanTooFastException extends RuntimeException {

    public ScanTooFastException() {
        super("Scan trop rapide, veuillez patienter 2 minutes");
    }
}