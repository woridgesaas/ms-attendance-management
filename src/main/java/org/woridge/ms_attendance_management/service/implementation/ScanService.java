package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woridge.ms_attendance_management.dto.rfid.EnrollRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;
import org.woridge.ms_attendance_management.enumeration.ScanMode;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardNotFoundException;
import org.woridge.ms_attendance_management.repository.RfidCardRepository;
import org.woridge.ms_attendance_management.strategy.rfid.resolver.ScanStrategyResolver;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ScanStrategyResolver resolver;
    private final RfidCardRepository rfidCardRepository;

    @Transactional
    public ScanResponse scan(ScanRequest request) {
        ScanResponse response = resolver.resolve().handle(request);
        if ("ENROLLED".equals(response.getAction())) {
            resolver.activateAttendanceMode();
        }
        return response;
    }

    public void activateEnrollmentMode(EnrollRequest request) {
        resolver.activateEnrollmentMode(request.getEmployeeId());
    }

    public void activateAttendanceMode() {
        resolver.activateAttendanceMode();
    }

    public ScanMode getCurrentMode() {
        return resolver.getCurrentMode();
    }

    // filtre sur active=true
    public boolean hasRfidCard(UUID employeeId) {
        return rfidCardRepository.existsByEmployeeIdAndActiveTrue(employeeId);
    }

    // désactivation de carte
    @Transactional
    public void deactivateCardByEmployee(UUID employeeId) {
        var card = rfidCardRepository
                .findActiveCardByEmployeeId(employeeId)
                .orElseThrow(() -> new RfidCardNotFoundException(employeeId.toString()));
        card.setActive(false);
        rfidCardRepository.save(card);
    }
}