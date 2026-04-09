package org.woridge.ms_attendance_management.enumeration;

public enum ErrorCode {

    // RFID
    RFID_CARD_NOT_FOUND,
    RFID_CARD_ALREADY_EXISTS,
    EMPLOYEE_ALREADY_HAS_CARD,
    SCAN_TOO_FAST,

    // Absence
    ABSENCE_NOT_FOUND,
    ABSENCE_FINAL_STATE,

    // Business config
    BUSINESS_PARAMETER_INVALID,

    //USER
    USERNOTFOUND,

    //OVERTIME
    OVERTIME_ALREADY_PROCESSED,
    OVERTIME_NOT_FOUND,

    //leave
    JUSTIFICATION_REQUIRED,
    INSUFFICIENT_BALANCE,
    INVALID_DATES,
    ACCESS_DENIED,
    LEAVES_NOT_FOUND

}
