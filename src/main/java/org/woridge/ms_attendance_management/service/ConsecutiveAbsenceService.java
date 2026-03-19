package org.woridge.ms_attendance_management.service;

import java.time.LocalDate;
import java.util.UUID;

public interface ConsecutiveAbsenceService {
    int calculate(UUID userId, LocalDate upToDate);
}
