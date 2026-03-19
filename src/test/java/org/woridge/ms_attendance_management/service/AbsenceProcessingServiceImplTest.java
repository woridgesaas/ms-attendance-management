package org.woridge.ms_attendance_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.woridge.ms_attendance_management.dto.absence.AbsenceProcessingRequest;
import org.woridge.ms_attendance_management.dto.absence.AbsenceResponse;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.handler.AbsenceHandler;
import org.woridge.ms_attendance_management.mapper.AbsenceMapper;
import org.woridge.ms_attendance_management.model.Absence;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.propreties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.AbsenceRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.implementation.AbsenceProcessingServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbsenceProcessingService — unit tests")
class AbsenceProcessingServiceImplTest {

    @Mock private AbsenceRepository         absenceRepository;
    @Mock private UserRepository            userRepository;
    @Mock private AbsenceHandler            absenceHandlerChain;
    @Mock private ConsecutiveAbsenceService consecutiveAbsenceService;
    @Mock private AlertService              alertService;
    @Mock private BusinessProperties        businessProperties;
    @Mock private AbsenceMapper             absenceMapper;

    @InjectMocks
    private AbsenceProcessingServiceImpl absenceProcessingService;

    private UUID      userId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        today  = LocalDate.now();
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : utilisateur non trouvé
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        AbsenceProcessingRequest request = new AbsenceProcessingRequest(userId, today, 0.0);

        assertThatThrownBy(() -> absenceProcessingService.process(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : utilisateur présent
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return null when user is present")
    void shouldReturnNullWhenUserIsPresent() {
        when(userRepository.existsById(userId)).thenReturn(true);

        // workedHours > 0 → return null directement sans toucher absenceRepository
        AbsenceProcessingRequest request = new AbsenceProcessingRequest(userId, today, 8.0);

        AbsenceResponse response = absenceProcessingService.process(request);

        assertThat(response).isNull();
        verify(absenceHandlerChain, never()).handle(any(), any());
        verify(absenceRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : absence finale → retourne l'absence existante sans exception
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return existing final absence without exception")
    void shouldReturnFinalAbsenceWithoutException() {
        Absence finalAbsence = buildAbsence(today, AbsenceType.UNJUSTIFIED_FINAL, true, 3);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today))
                .thenReturn(Optional.of(finalAbsence));
        when(absenceMapper.toAbsenceResponse(finalAbsence))
                .thenReturn(buildAbsenceResponse(finalAbsence));

        AbsenceProcessingRequest request = new AbsenceProcessingRequest(userId, today, 0.0);
        AbsenceResponse response = absenceProcessingService.process(request);

        // ✅ pas d'exception — on retourne l'absence finale telle quelle
        assertThat(response).isNotNull();
        assertThat(response.absenceType()).isEqualTo(AbsenceType.UNJUSTIFIED_FINAL);
        assertThat(response.isFinal()).isTrue();

        // ✅ on ne retraite pas — pas d'appel au handler
        verify(absenceHandlerChain, never()).handle(any(), any());
        verify(absenceRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : JUSTIFIED_LEAVE → consecutiveDays = 0 directement
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should classify as JUSTIFIED_LEAVE and not trigger alert")
    void shouldClassifyAsJustifiedLeave() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today))
                .thenReturn(Optional.empty());
        when(absenceHandlerChain.handle(userId, today))
                .thenReturn(AbsenceType.JUSTIFIED_LEAVE);

        // ✅ JUSTIFIED_LEAVE → isJustified() = true → consecutiveDays = 0 directement
        // → pas besoin de stubber getCeoAlertThreshold ni calculate

        Absence saved = buildAbsence(today, AbsenceType.JUSTIFIED_LEAVE, false, 0);
        when(absenceRepository.save(any())).thenReturn(saved);
        when(absenceMapper.toAbsenceResponse(saved)).thenReturn(buildAbsenceResponse(saved));
        when(userRepository.getReferenceById(userId)).thenReturn(buildMockUser());

        AbsenceProcessingRequest request = new AbsenceProcessingRequest(userId, today, 0.0);
        AbsenceResponse response = absenceProcessingService.process(request);

        assertThat(response).isNotNull();
        assertThat(response.absenceType()).isEqualTo(AbsenceType.JUSTIFIED_LEAVE);
        assertThat(response.consecutiveDays()).isEqualTo(0);
        verify(alertService, never()).evaluate(any(), anyInt());
        verify(consecutiveAbsenceService, never()).calculate(any(), any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : UNJUSTIFIED + alerte RH
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should classify as UNJUSTIFIED and trigger RH alert")
    void shouldClassifyAsUnjustifiedAndTriggerRhAlert() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today))
                .thenReturn(Optional.empty());
        when(absenceHandlerChain.handle(userId, today))
                .thenReturn(AbsenceType.UNJUSTIFIED);
        when(consecutiveAbsenceService.calculate(userId, today))
                .thenReturn(2);
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(userRepository.getReferenceById(userId)).thenReturn(buildMockUser());

        Absence saved = buildAbsence(today, AbsenceType.UNJUSTIFIED, false, 2);
        when(absenceRepository.save(any())).thenReturn(saved);
        when(absenceMapper.toAbsenceResponse(saved)).thenReturn(buildAbsenceResponse(saved));

        AbsenceProcessingRequest request = new AbsenceProcessingRequest(userId, today, 0.0);
        absenceProcessingService.process(request);

        verify(alertService).evaluate(userId, 2);
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : UNJUSTIFIED_FINAL
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should mark as UNJUSTIFIED_FINAL when CEO threshold reached")
    void shouldMarkAsUnjustifiedFinalWhenCeoThresholdReached() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(absenceRepository.findByUserIdAndAbsenceDate(userId, today))
                .thenReturn(Optional.empty());
        when(absenceHandlerChain.handle(userId, today))
                .thenReturn(AbsenceType.UNJUSTIFIED);
        when(consecutiveAbsenceService.calculate(userId, today))
                .thenReturn(3);
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(userRepository.getReferenceById(userId)).thenReturn(buildMockUser());

        Absence saved = buildAbsence(today, AbsenceType.UNJUSTIFIED_FINAL, true, 3);
        when(absenceRepository.save(any())).thenReturn(saved);
        when(absenceMapper.toAbsenceResponse(saved)).thenReturn(buildAbsenceResponse(saved));

        AbsenceProcessingRequest request = new AbsenceProcessingRequest(userId, today, 0.0);
        absenceProcessingService.process(request);

        // ✅ On vérifie que applyUpdate a été appelé avec isFinal=true et UNJUSTIFIED_FINAL
        verify(absenceMapper).applyUpdate(
                any(Absence.class),
                eq(AbsenceType.UNJUSTIFIED_FINAL),
                eq(true),
                eq(3)
        );

        // ✅ On vérifie que save a bien été appelé
        verify(absenceRepository).save(any(Absence.class));
    }
    // ─────────────────────────────────────────────────────────────
    // Cas : getAbsencesByUser
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return absence list for existing user")
    void shouldReturnAbsenceListForUser() {
        when(userRepository.existsById(userId)).thenReturn(true);

        List<Absence> absences = List.of(
                buildAbsence(today, AbsenceType.UNJUSTIFIED, false, 1),
                buildAbsence(today.minusDays(1), AbsenceType.JUSTIFIED_LEAVE, false, 0)
        );
        when(absenceRepository.findByUserIdOrderByAbsenceDateDesc(userId))
                .thenReturn(absences);
        when(absenceMapper.toAbsenceResponseList(absences))
                .thenReturn(List.of(
                        buildAbsenceResponse(absences.get(0)),
                        buildAbsenceResponse(absences.get(1))
                ));

        List<AbsenceResponse> result = absenceProcessingService.getAbsencesByUser(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting absences for unknown user")
    void shouldThrowWhenGettingAbsencesForUnknownUser() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> absenceProcessingService.getAbsencesByUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private User buildMockUser() {
        return User.builder()
                .id(userId)
                .build();
    }

    private Absence buildAbsence(LocalDate date, AbsenceType type,
                                 boolean isFinal, int consecutiveDays) {
        return Absence.builder()
                .id(UUID.randomUUID())
                .user(buildMockUser())
                .absenceDate(date)
                .absenceType(type)
                .isFinal(isFinal)
                .consecutiveDays(consecutiveDays)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AbsenceResponse buildAbsenceResponse(Absence absence) {
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
}