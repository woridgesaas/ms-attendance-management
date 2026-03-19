package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.enumeration.AlertResult;

import java.util.UUID;

public interface AlertService {
    AlertResult evaluate(UUID userId, int consecutiveDays);

}