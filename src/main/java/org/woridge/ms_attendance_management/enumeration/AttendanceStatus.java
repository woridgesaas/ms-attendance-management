package org.woridge.ms_attendance_management.enumeration;

public enum AttendanceStatus {

    PRESENT,
    ABSENT,
    LATE,//Employee checked in later than the allowed standard time.
    EARLY_LEAVE,//Employee left earlier than the expected end time
    HALF_DAY,//Employee worked only a partial day
    UNDER_TIME,//Employee worked less than the minimum expected hours
    OVERTIME,
    WEEKEND,//Day is a weekend and no normal attendance is expected
    HOLIDAY

}