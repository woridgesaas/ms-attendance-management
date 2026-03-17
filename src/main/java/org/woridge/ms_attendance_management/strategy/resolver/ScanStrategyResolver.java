package org.woridge.ms_attendance_management.strategy.resolver;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.ScanMode;
import org.woridge.ms_attendance_management.strategy.ScanStrategy;
import org.woridge.ms_attendance_management.strategy.implementation.AttendanceStrategyImpl;
import org.woridge.ms_attendance_management.strategy.implementation.EnrollmentStrategyImpl;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScanStrategyResolver {

    private final AttendanceStrategyImpl attendanceStrategy;
    private final EnrollmentStrategyImpl enrollmentStrategy;

    // Mode par défaut : pointage normal
    private ScanMode currentMode = ScanMode.ATTENDANCE;

    // Retourne la bonne stratégie selon le mode actuel
    public ScanStrategy resolve() {
        return switch (currentMode) {
            case ATTENDANCE -> attendanceStrategy;
            case ENROLLMENT -> enrollmentStrategy;
        };
    }

    // Appelé par le controller quand l'admin active le mode enrollment
    public void activateEnrollmentMode(UUID employeeId) {
        enrollmentStrategy.setPendingEmployeeId(employeeId);
        this.currentMode = ScanMode.ENROLLMENT;
    }

    // Appelé par le controller pour revenir au mode normal
    public void activateAttendanceMode() {
        this.currentMode = ScanMode.ATTENDANCE;
    }

    public ScanMode getCurrentMode() {
        return currentMode;
    }
}