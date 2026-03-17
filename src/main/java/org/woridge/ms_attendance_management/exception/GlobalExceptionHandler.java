package org.woridge.ms_attendance_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.woridge.ms_attendance_management.exception.rfid.EmployeeAlreadyHasCardException;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardAlreadyExistsException;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardNotFoundException;
import org.woridge.ms_attendance_management.exception.rfid.ScanTooFastException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice  // intercepte toutes les exceptions de tous les controllers
public class GlobalExceptionHandler {

    // Carte inconnue → 404
    @ExceptionHandler(RfidCardNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCardNotFound(
            RfidCardNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Carte déjà enregistrée → 409 Conflict
    @ExceptionHandler(RfidCardAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleCardAlreadyExists(
            RfidCardAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Employé a déjà une carte → 409 Conflict
    @ExceptionHandler(EmployeeAlreadyHasCardException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeAlreadyHasCard(
            EmployeeAlreadyHasCardException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Scan trop rapide → 429 Too Many Requests
    @ExceptionHandler(ScanTooFastException.class)
    public ResponseEntity<Map<String, Object>> handleScanTooFast(
            ScanTooFastException ex) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    // Toute autre exception non gérée → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue");
    }

    // Méthode utilitaire — construit la réponse JSON
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}