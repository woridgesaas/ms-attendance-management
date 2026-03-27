package org.woridge.ms_attendance_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.repository.OvertimeRepository;
import org.woridge.ms_attendance_management.service.implementation.OvertimeProcessingServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class OvertimeProcessingServiceTest {
    @Mock
    OvertimeRepository overtimeRepository;
    @InjectMocks
    OvertimeProcessingServiceImpl overtimeProcessingService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    // Friday end in 10PM
    void testDetectAndCreate_WeekdayOvertime() {

        WorkingDay workingDay = new WorkingDay();
        workingDay.setDate(LocalDate.of(2026, 3, 20));
        workingDay.setCheckOutTime(LocalTime.MAX.of(22, 0)); // 22h
        var id = UUID.randomUUID();
        workingDay.setPerformedBy(new User(id));

        // Act
        overtimeProcessingService.detectAndCreate(workingDay);


        verify(overtimeRepository, times(1)).save(argThat(overtime -> {
            assertEquals(workingDay, overtime.getWorkingDay());
            assertEquals(id, overtime.getUser().getId());
            assertEquals(OvertimeStatus.PENDING , overtime.getStatus());
            assertEquals(3.0, overtime.getHours25(), 0.01);
            assertEquals(1.0, overtime.getHours50(), 0.01);
            assertEquals(0.0, overtime.getHours100(), 0.01);

            return true;
        }));
    }

    //Sunday 50% & 100% just
    @Test
    void testDetectAndCreate_WeekendOvertime() {
        // Arrange : dimanche, sortie à 23h
        WorkingDay workingDay = new WorkingDay();
        workingDay.setDate(LocalDate.of(2026, 3, 22));
        workingDay.setCheckOutTime(LocalTime.of(23, 0));
        var id = UUID.randomUUID();
        workingDay.setPerformedBy(new User(id));

        // Act
        overtimeProcessingService.detectAndCreate(workingDay);

        // Assert
        verify(overtimeRepository).save(argThat(overtime -> {
            assertEquals(workingDay, overtime.getWorkingDay());
            assertEquals(id, overtime.getUser().getId());
            assertEquals(OvertimeStatus.PENDING , overtime.getStatus());
            assertEquals(0.0, overtime.getHours25(), 0.01);
            assertEquals(3.0,overtime.getHours50());
            assertEquals(2.0, overtime.getHours100(), 0.01);

            return true;
        }));
    }

}
