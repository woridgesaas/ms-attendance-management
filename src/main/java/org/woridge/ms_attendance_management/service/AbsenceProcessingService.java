package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.dto.absence.AbsenceProcessingRequest;
import org.woridge.ms_attendance_management.dto.absence.AbsenceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AbsenceProcessingService {
    AbsenceResponse process(AbsenceProcessingRequest request);
    List<AbsenceResponse> getAbsencesByUser(UUID userId);
    AbsenceResponse getAbsenceByUserAndDate(UUID userId, LocalDate date);
}
