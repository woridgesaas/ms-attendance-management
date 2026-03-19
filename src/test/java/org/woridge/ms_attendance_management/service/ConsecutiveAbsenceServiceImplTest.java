package org.woridge.ms_attendance_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.model.Absence;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.repository.AbsenceRepository;
import org.woridge.ms_attendance_management.service.implementation.ConsecutiveAbsenceServiceImpl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsecutiveAbsenceService — unit tests")
class ConsecutiveAbsenceServiceImplTest {

    @Mock
    private AbsenceRepository absenceRepository;

    @InjectMocks
    private ConsecutiveAbsenceServiceImpl consecutiveAbsenceService;

    private UUID userId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        today  = LocalDate.now();
    }

    // ─────────────────────────────────────────────────────────────
    // Rappel : calculate() commence depuis upToDate.minusDays(1)
    // Le jour actuel n'est pas encore en base → on le compte
    // automatiquement (count = 0 avant boucle)
    // On remonte depuis hier vers le passé
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return 0 when no absence found yesterday")
    void shouldReturnZeroWhenNoAbsenceYesterday() {
        // calculate(today) → cherche depuis today-1 (hier)
        // hier → pas d'absence → count = 0
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(1)))
                .thenReturn(Optional.empty());

        int result = consecutiveAbsenceService.calculate(userId, today);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("Should return 1 when one unjustified absence found yesterday")
    void shouldReturnOneWhenOneUnjustifiedAbsence() {
        // calculate(today) → cherche depuis today-1
        // hier → UNJUSTIFIED → count = 1
        // avant-hier → pas d'absence → stop
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(1)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(1), AbsenceType.UNJUSTIFIED)));
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(2)))
                .thenReturn(Optional.empty());

        int result = consecutiveAbsenceService.calculate(userId, today);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return 3 when three consecutive unjustified absences before today")
    void shouldReturnThreeWhenThreeConsecutiveAbsences() {
        // calculate(today) → cherche depuis today-1
        // today-1, today-2, today-3 → UNJUSTIFIED → count = 3
        // today-4 → pas d'absence → stop
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(1)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(1), AbsenceType.UNJUSTIFIED)));
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(2)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(2), AbsenceType.UNJUSTIFIED)));
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(3)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(3), AbsenceType.UNJUSTIFIED)));
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(4)))
                .thenReturn(Optional.empty());

        int result = consecutiveAbsenceService.calculate(userId, today);

        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("Should stop chain when justified leave found yesterday")
    void shouldStopChainWhenJustifiedAbsence() {
        // calculate(today) → cherche depuis today-1
        // hier → JUSTIFIED_LEAVE → stop → count = 0
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(1)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(1), AbsenceType.JUSTIFIED_LEAVE)));

        int result = consecutiveAbsenceService.calculate(userId, today);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("Should stop chain when justified justification found yesterday")
    void shouldStopChainWhenJustifiedJustification() {
        // calculate(today) → cherche depuis today-1
        // hier → JUSTIFIED_JUSTIFICATION → stop → count = 0
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(1)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(1), AbsenceType.JUSTIFIED_JUSTIFICATION)));

        int result = consecutiveAbsenceService.calculate(userId, today);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("Should count 1 unjustified then stop when justified found")
    void shouldCountOneUnjustifiedThenStop() {
        // calculate(today) → cherche depuis today-1
        // today-1 → UNJUSTIFIED → count = 1
        // today-2 → JUSTIFIED_LEAVE → stop
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(1)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(1), AbsenceType.UNJUSTIFIED)));
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today.minusDays(2)))
                .thenReturn(Optional.of(buildAbsence(today.minusDays(2), AbsenceType.JUSTIFIED_LEAVE)));

        int result = consecutiveAbsenceService.calculate(userId, today);

        assertThat(result).isEqualTo(1);
    }


    private User buildMockUser() {
        return User.builder()
                .id(userId)
                .build();
    }

    private Absence buildAbsence(LocalDate date, AbsenceType type) {
        return Absence.builder()
                .id(UUID.randomUUID())
                .user(buildMockUser())
                .absenceDate(date)
                .absenceType(type)
                .isFinal(false)
                .consecutiveDays(0)
                .build();
    }
}