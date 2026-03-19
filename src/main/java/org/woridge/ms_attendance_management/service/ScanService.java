package org.woridge.ms_attendance_management.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woridge.ms_attendance_management.dto.rfid.EnrollRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;
import org.woridge.ms_attendance_management.enumeration.ScanMode;
import org.woridge.ms_attendance_management.repository.RfidCardRepository;
import org.woridge.ms_attendance_management.strategy.rfid.resolver.ScanStrategyResolver;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ScanStrategyResolver resolver;
    private final RfidCardRepository rfidCardRepository;

    // Appelé à chaque scan ESP8266
    // @Transactional garantit que si une erreur survient
    // pendant la sauvegarde → tout est annulé (rollback)
    @Transactional
    public ScanResponse scan(ScanRequest request) {

        ScanResponse response = resolver.resolve().handle(request);
        if ("ENROLLED".equals(response.getAction())) {
            resolver.activateAttendanceMode();
        }
        return response;
    }

    // Appelé par l'admin pour activer le mode enrollment
    public void activateEnrollmentMode(EnrollRequest request) {
        resolver.activateEnrollmentMode(request.getEmployeeId());
    }

    // Appelé par l'admin pour revenir au mode normal
    public void activateAttendanceMode() {
        resolver.activateAttendanceMode();
    }

    // Voir le mode actuel
    public ScanMode getCurrentMode() {
        return resolver.getCurrentMode();
    }
    // Vérifier si un employé a une carte RFID associée
    public boolean hasRfidCard(UUID employeeId) {
        return rfidCardRepository.existsByEmployeeId(employeeId);
    }
}
