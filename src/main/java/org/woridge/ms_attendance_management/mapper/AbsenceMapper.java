package org.woridge.ms_attendance_management.mapper;

import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.absence.AbsenceHistoryResponse;
import org.woridge.ms_attendance_management.dto.absence.AbsenceResponse;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.model.Absence;

import java.util.List;

@Component
public class AbsenceMapper {


    public AbsenceResponse toAbsenceResponse(Absence absence) {
        return new AbsenceResponse(
                absence.getId(),
                absence.getUser().getId(),
                absence.getAbsenceDate(),
                absence.getAbsenceType(),
                absence.isFinal(),
                absence.getConsecutiveDays(),
                absence.getCreatedAt(),
                absence.getUpdatedAt()
        );
    }

    public AbsenceHistoryResponse toAbsenceHistoryResponse(Absence absence) {
        return new AbsenceHistoryResponse(
                absence.getId(),
                absence.getAbsenceDate(),
                absence.getAbsenceType(),
                absence.isFinal(),
                absence.getConsecutiveDays(),
                absence.getCreatedAt()
        );
    }

    public List<AbsenceResponse> toAbsenceResponseList(List<Absence> absences) {
        if (absences == null) return List.of();
        return absences.stream()
                .map(this::toAbsenceResponse)
                .toList();
    }

    //future
    public List<AbsenceHistoryResponse> toAbsenceHistoryResponseList(List<Absence> absences) {
        if (absences == null) return List.of();
        return absences.stream()
                .map(this::toAbsenceHistoryResponse)
                .toList();
    }

    // ─── Met à jour les champs modifiables d'une absence ──────────
    // id, user, absenceDate, createdAt → jamais modifiés

    public void applyUpdate(Absence absence, AbsenceType type,
                            boolean isFinal, int consecutiveDays) {
        absence.setAbsenceType(type);
        absence.setFinal(isFinal);
        absence.setConsecutiveDays(consecutiveDays);
    }
}