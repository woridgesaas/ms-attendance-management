package org.woridge.ms_attendance_management.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woridge.ms_attendance_management.dto.rfid.EnrollRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;
import org.woridge.ms_attendance_management.enumeration.ScanMode;
import org.woridge.ms_attendance_management.repository.RfidCardRepository;
import org.woridge.ms_attendance_management.service.ScanService;

import java.util.UUID;

@RestController
@RequestMapping("/rfid")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;


    // ✅ Appelé par l'ESP8266 à chaque scan de carte
    // POST /api/rfid/scan
    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@RequestBody ScanRequest request) {
        ScanResponse response = scanService.scan(request);
        return ResponseEntity.ok(response);
    }

    // ✅ Admin active le mode enrollment pour un employé
    // PUT /api/rfid/mode/enrollment
    @PutMapping("/mode/enrollment")
    public ResponseEntity<String> activateEnrollmentMode(
            @RequestBody EnrollRequest request) {
        scanService.activateEnrollmentMode(request);
        return ResponseEntity.ok(
                "Mode enrollment activé pour l'employé : " + request.getEmployeeId()
        );
    }

    // ✅ Admin revient au mode pointage normal
    // PUT /api/rfid/mode/attendance
    @PutMapping("/mode/attendance")
    public ResponseEntity<String> activateAttendanceMode() {
        scanService.activateAttendanceMode();
        return ResponseEntity.ok("Mode attendance activé");
    }

    // ✅ Voir le mode actuel
    // GET /api/rfid/mode
    @GetMapping("/mode")
    public ResponseEntity<ScanMode> getCurrentMode() {
        return ResponseEntity.ok(scanService.getCurrentMode());
    }
    // Vérifier si un employé a une carte RFID associée
    // Vérifier si un employé a une carte RFID associée
    @GetMapping("/enrollment/status/{employeeId}")
    public ResponseEntity<Boolean> checkEnrollmentStatus(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(scanService.hasRfidCard(employeeId));
    }
}
