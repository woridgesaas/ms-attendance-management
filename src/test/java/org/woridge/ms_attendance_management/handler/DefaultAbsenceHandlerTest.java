package org.woridge.ms_attendance_management.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.strategy.absence.implementation.DefaultClassificationStrategy;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultAbsenceHandler — unit tests")
class DefaultAbsenceHandlerTest {

    @Mock
    private DefaultClassificationStrategy strategy;

    @InjectMocks
    private DefaultAbsenceHandler handler;

    private UUID      userId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        today  = LocalDate.now();
    }

    @Test
    @DisplayName("Should always return UNJUSTIFIED as end of chain")
    void shouldAlwaysReturnUnjustified() {
        when(strategy.classify(userId, today)).thenReturn(AbsenceType.UNJUSTIFIED);

        AbsenceType result = handler.handle(userId, today);

        assertThat(result).isEqualTo(AbsenceType.UNJUSTIFIED);
    }
}
