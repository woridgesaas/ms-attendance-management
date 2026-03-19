package org.woridge.ms_attendance_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;
import org.woridge.ms_attendance_management.enumeration.AlertResult;
import org.woridge.ms_attendance_management.model.AbsenceAlert;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.propreties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.AbsenceAlertRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.implementation.AlertServiceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService — unit tests")
class AlertServiceImplTest {

    @Mock
    private AbsenceAlertRepository absenceAlertRepository;

    @Mock
    private BusinessProperties businessProperties;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlertServiceImpl alertService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Mock du User retourné par getReferenceById
        mockUser = User.builder()
                .id(userId)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : en dessous des seuils
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return NO_THRESHOLD when below RH threshold")
    void shouldNotTriggerAlertBelowRhThreshold() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(businessProperties.getRhAlertThreshold()).thenReturn(2);

        AlertResult result = alertService.evaluate(userId, 1);

        assertThat(result).isEqualTo(AlertResult.NO_THRESHOLD);
        verify(absenceAlertRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : seuil RH atteint
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should trigger RH alert and return TRIGGERED")
    void shouldTriggerRhAlertWhenThresholdReached() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(businessProperties.getRhAlertThreshold()).thenReturn(2);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.RH), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

        AlertResult result = alertService.evaluate(userId, 2);

        assertThat(result).isEqualTo(AlertResult.TRIGGERED);

        ArgumentCaptor<AbsenceAlert> captor = ArgumentCaptor.forClass(AbsenceAlert.class);
        verify(absenceAlertRepository).save(captor.capture());

        assertThat(captor.getValue().getAlertLevel()).isEqualTo(AlertLevel.RH);
        assertThat(captor.getValue().getUser().getId()).isEqualTo(userId); // ✅ corrigé
        assertThat(captor.getValue().getConsecutiveDays()).isEqualTo(2);
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : seuil CEO atteint
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should trigger CEO alert and return TRIGGERED")
    void shouldTriggerCeoAlertWhenThresholdReached() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.CEO), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

        AlertResult result = alertService.evaluate(userId, 3);

        assertThat(result).isEqualTo(AlertResult.TRIGGERED);

        ArgumentCaptor<AbsenceAlert> captor = ArgumentCaptor.forClass(AbsenceAlert.class);
        verify(absenceAlertRepository).save(captor.capture());

        assertThat(captor.getValue().getAlertLevel()).isEqualTo(AlertLevel.CEO);
        assertThat(captor.getValue().getUser().getId()).isEqualTo(userId); // ✅ corrigé
        assertThat(captor.getValue().getConsecutiveDays()).isEqualTo(3);
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : alerte RH déjà déclenchée aujourd'hui
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return SKIPPED_ALREADY_TRIGGERED when RH alert already triggered today")
    void shouldNotTriggerDuplicateRhAlert() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(businessProperties.getRhAlertThreshold()).thenReturn(2);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.RH), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        AlertResult result = alertService.evaluate(userId, 2);

        assertThat(result).isEqualTo(AlertResult.SKIPPED_ALREADY_TRIGGERED);
        verify(absenceAlertRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : CEO threshold — une seule alerte déclenchée
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should trigger only CEO alert when CEO threshold is reached")
    void shouldTriggerOnlyCeoAlertWhenCeoThresholdReached() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.CEO), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

        AlertResult result = alertService.evaluate(userId, 5);

        assertThat(result).isEqualTo(AlertResult.TRIGGERED);

        ArgumentCaptor<AbsenceAlert> captor = ArgumentCaptor.forClass(AbsenceAlert.class);
        verify(absenceAlertRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getAlertLevel()).isEqualTo(AlertLevel.CEO);
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : CEO already triggered
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return SKIPPED_ALREADY_TRIGGERED when CEO alert already triggered today")
    void shouldNotTriggerDuplicateCeoAlert() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.CEO), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        AlertResult result = alertService.evaluate(userId, 3);

        assertThat(result).isEqualTo(AlertResult.SKIPPED_ALREADY_TRIGGERED);
        verify(absenceAlertRepository, never()).save(any());
    }
}