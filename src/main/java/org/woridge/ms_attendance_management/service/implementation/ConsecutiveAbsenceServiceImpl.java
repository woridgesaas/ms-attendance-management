package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.model.Absence;
import org.woridge.ms_attendance_management.repository.AbsenceRepository;
import org.woridge.ms_attendance_management.service.ConsecutiveAbsenceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsecutiveAbsenceServiceImpl implements ConsecutiveAbsenceService {

    private final AbsenceRepository absenceRepository;

    private static final List<AbsenceType> BREAK_CHAIN_TYPES = List.of(
            AbsenceType.JUSTIFIED_LEAVE,
            AbsenceType.JUSTIFIED_JUSTIFICATION
    );

    @Override
    public int calculate(UUID userId, LocalDate upToDate) {
        int count = 0;
        LocalDate current = upToDate.minusDays(1); // ← on commence depuis HIER

        while (true) {
            Optional<Absence> absenceOpt = absenceRepository
                    .findByUserIdAndAbsenceDate(userId, current);

            if (absenceOpt.isEmpty()) {
                break;
            }

            AbsenceType type = absenceOpt.get().getAbsenceType();

            if (BREAK_CHAIN_TYPES.contains(type)) {
                break;
            }

            count++;
            current = current.minusDays(1);
        }

        return count;
    }
}
