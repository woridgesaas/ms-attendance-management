package org.woridge.ms_attendance_management.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.strategy.absence.implementation.JustificationClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JustificationAbsenceHandler — unit tests")
class JustificationAbsenceHandlerTest {

    @Mock
    private JustificationClassificationStrategy strategy;

    @Mock
    private AbsenceHandler nextHandler;

    @InjectMocks
    private JustificationAbsenceHandler handler;

    private UUID      userId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        today  = LocalDate.now();
        handler.setNext(nextHandler);
    }

    @Test
    @DisplayName("Should return JUSTIFIED_JUSTIFICATION when justification approved")
    void shouldReturnJustifiedJustification() {
        when(strategy.canHandle(userId, today)).thenReturn(true);
        when(strategy.classify(userId, today)).thenReturn(AbsenceType.JUSTIFIED_JUSTIFICATION);

        AbsenceType result = handler.handle(userId, today);

        assertThat(result).isEqualTo(AbsenceType.JUSTIFIED_JUSTIFICATION);
        verify(nextHandler, never()).handle(any(), any());
    }

    @Test
    @DisplayName("Should return PENDING_JUSTIFICATION when justification pending")
    void shouldReturnPendingJustification() {
        when(strategy.canHandle(userId, today)).thenReturn(true);
        when(strategy.classify(userId, today)).thenReturn(AbsenceType.PENDING_JUSTIFICATION);

        AbsenceType result = handler.handle(userId, today);

        assertThat(result).isEqualTo(AbsenceType.PENDING_JUSTIFICATION);
    }

    @Test
    @DisplayName("Should pass to next when no justification found")
    void shouldPassToNextWhenNoJustification() {
        when(strategy.canHandle(userId, today)).thenReturn(false);
        when(nextHandler.handle(userId, today)).thenReturn(AbsenceType.UNJUSTIFIED);

        AbsenceType result = handler.handle(userId, today);

        assertThat(result).isEqualTo(AbsenceType.UNJUSTIFIED);
        verify(nextHandler).handle(userId, today);
    }
}
