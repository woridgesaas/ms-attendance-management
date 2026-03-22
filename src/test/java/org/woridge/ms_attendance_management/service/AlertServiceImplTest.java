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
import org.woridge.ms_attendance_management.messaging.event.AbsenceAlertEvent;
import org.woridge.ms_attendance_management.messaging.publisher.AbsenceAlertPublisher;
import org.woridge.ms_attendance_management.model.AbsenceAlert;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.properties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.AbsenceAlertRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.implementation.AlertServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService — unit tests")
class AlertServiceImplTest {

    @Mock private AbsenceAlertRepository absenceAlertRepository;
    @Mock private BusinessProperties     businessProperties;
    @Mock private UserRepository         userRepository;
    @Mock private AbsenceAlertPublisher absenceAlertPublisher; // ✅ interface

    @InjectMocks
    private AlertServiceImpl alertService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .firstName("Ahmed")
                .lastName("Benali")
                .email("ahmed.benali@woridge.com")
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
        verify(absenceAlertPublisher, never()).publish(any()); // ✅ une seule méthode
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : seuil RH atteint
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should trigger RH alert, save in DB and publish event")
    void shouldTriggerRhAlertWhenThresholdReached() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(businessProperties.getRhAlertThreshold()).thenReturn(2);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.RH), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        AlertResult result = alertService.evaluate(userId, 2);

        assertThat(result).isEqualTo(AlertResult.TRIGGERED);

        // ✅ sauvegarde en base
        ArgumentCaptor<AbsenceAlert> alertCaptor = ArgumentCaptor.forClass(AbsenceAlert.class);
        verify(absenceAlertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getAlertLevel()).isEqualTo(AlertLevel.RH);
        assertThat(alertCaptor.getValue().getUser().getId()).isEqualTo(userId);
        assertThat(alertCaptor.getValue().getConsecutiveDays()).isEqualTo(2);

        // ✅ publisher appelé avec AbsenceAlertEvent niveau RH
        ArgumentCaptor<AbsenceAlertEvent> eventCaptor =
                ArgumentCaptor.forClass(AbsenceAlertEvent.class);
        verify(absenceAlertPublisher).publish(eventCaptor.capture());

        AbsenceAlertEvent event = eventCaptor.getValue();
        assertThat(event.employeeFullName()).isEqualTo("Ahmed Benali");
        assertThat(event.employeeEmail()).isEqualTo("ahmed.benali@woridge.com");
        assertThat(event.consecutiveDays()).isEqualTo(2);
        assertThat(event.alertLevel()).isEqualTo(AlertLevel.RH); // ✅ vérifier le niveau
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : seuil CEO atteint
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should trigger CEO alert, save in DB and publish event")
    void shouldTriggerCeoAlertWhenThresholdReached() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.CEO), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        AlertResult result = alertService.evaluate(userId, 3);

        assertThat(result).isEqualTo(AlertResult.TRIGGERED);

        // ✅ sauvegarde en base
        ArgumentCaptor<AbsenceAlert> alertCaptor = ArgumentCaptor.forClass(AbsenceAlert.class);
        verify(absenceAlertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getAlertLevel()).isEqualTo(AlertLevel.CEO);
        assertThat(alertCaptor.getValue().getUser().getId()).isEqualTo(userId);
        assertThat(alertCaptor.getValue().getConsecutiveDays()).isEqualTo(3);

        // ✅ publisher appelé avec AbsenceAlertEvent niveau CEO
        ArgumentCaptor<AbsenceAlertEvent> eventCaptor =
                ArgumentCaptor.forClass(AbsenceAlertEvent.class);
        verify(absenceAlertPublisher).publish(eventCaptor.capture());

        AbsenceAlertEvent event = eventCaptor.getValue();
        assertThat(event.employeeFullName()).isEqualTo("Ahmed Benali");
        assertThat(event.employeeEmail()).isEqualTo("ahmed.benali@woridge.com");
        assertThat(event.consecutiveDays()).isEqualTo(3);
        assertThat(event.alertLevel()).isEqualTo(AlertLevel.CEO); // ✅ vérifier le niveau
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : alerte RH déjà déclenchée aujourd'hui
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return SKIPPED and not publish when RH alert already triggered today")
    void shouldNotTriggerDuplicateRhAlert() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(businessProperties.getRhAlertThreshold()).thenReturn(2);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.RH), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        AlertResult result = alertService.evaluate(userId, 2);

        assertThat(result).isEqualTo(AlertResult.SKIPPED_ALREADY_TRIGGERED);
        verify(absenceAlertRepository, never()).save(any());
        verify(absenceAlertPublisher, never()).publish(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : alerte CEO déjà déclenchée aujourd'hui
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return SKIPPED and not publish when CEO alert already triggered today")
    void shouldNotTriggerDuplicateCeoAlert() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.CEO), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        AlertResult result = alertService.evaluate(userId, 3);

        assertThat(result).isEqualTo(AlertResult.SKIPPED_ALREADY_TRIGGERED);
        verify(absenceAlertRepository, never()).save(any());
        verify(absenceAlertPublisher, never()).publish(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Cas : CEO threshold — une seule alerte déclenchée (pas RH)
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should trigger only CEO alert when CEO threshold is reached")
    void shouldTriggerOnlyCeoAlertWhenCeoThresholdReached() {
        when(businessProperties.getCeoAlertThreshold()).thenReturn(3);
        when(absenceAlertRepository.existsByUserIdAndAlertLevelAndTriggeredAtBetween(
                eq(userId), eq(AlertLevel.CEO), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        AlertResult result = alertService.evaluate(userId, 5);

        assertThat(result).isEqualTo(AlertResult.TRIGGERED);

        // ✅ une seule sauvegarde
        verify(absenceAlertRepository, times(1)).save(any());

        // ✅ publish appelé une seule fois avec niveau CEO
        ArgumentCaptor<AbsenceAlertEvent> captor =
                ArgumentCaptor.forClass(AbsenceAlertEvent.class);
        verify(absenceAlertPublisher, times(1)).publish(captor.capture());
        assertThat(captor.getValue().alertLevel()).isEqualTo(AlertLevel.CEO);
    }
}