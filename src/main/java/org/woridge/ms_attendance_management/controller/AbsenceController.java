package org.woridge.ms_attendance_management.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woridge.ms_attendance_management.dto.SuccessResponse;
import org.woridge.ms_attendance_management.dto.absence.AbsenceProcessingRequest;
import org.woridge.ms_attendance_management.dto.absence.AbsenceResponse;
import org.woridge.ms_attendance_management.service.AbsenceProcessingService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceProcessingService absenceProcessingService;

    // Traitement principal : détecte et classifie une absence
    @PostMapping
    public ResponseEntity<SuccessResponse<AbsenceResponse>> process(
            @Valid @RequestBody AbsenceProcessingRequest request) {

        AbsenceResponse response = absenceProcessingService.process(request);

        // Utilisateur présent → data null(future:working day)
        if (response == null) {
            return ResponseEntity.ok(
                    SuccessResponse.createSuccessResponse(
                            "User is present.",
                            null
                    )
            );
        }

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Attendance processed successfully.",
                        response
                )
        );
    }
    // Historique complet des absences d'un utilisateur

    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<List<AbsenceResponse>>> getAbsencesByUser(
            @PathVariable UUID userId) {

        List<AbsenceResponse> absences = absenceProcessingService
                .getAbsencesByUser(userId);

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Absences retrieved successfully.",
                        absences
                )
        );
    }

    // Absence d'un utilisateur à une date précise
    @GetMapping("/{userId}/{date}")
    public ResponseEntity<SuccessResponse<AbsenceResponse>> getAbsenceByUserAndDate(
            @PathVariable UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AbsenceResponse absence = absenceProcessingService
                .getAbsenceByUserAndDate(userId, date);

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "Absence retrieved successfully.",
                        absence
                )
        );
    }
}
