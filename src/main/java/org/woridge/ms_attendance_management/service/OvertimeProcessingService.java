package org.woridge.ms_attendance_management.service;

import jakarta.transaction.Transactional;
import org.woridge.ms_attendance_management.model.WorkingDay;

import java.time.LocalTime;

public interface OvertimeProcessingService {
    //Future ms-settings
     static final LocalTime DEFAULT_END_TIME = LocalTime.of(18, 0); // 18h00
     static final LocalTime NIGHT_START_TIME = LocalTime.of(21, 0); // 21h00
    static final int MIN_OVERTIME_MINUTES = 30;


    @Transactional
    public void detectAndCreate(WorkingDay workingDay);
}
