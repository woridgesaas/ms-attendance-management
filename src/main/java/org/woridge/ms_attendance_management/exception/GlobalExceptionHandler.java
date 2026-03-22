package org.woridge.ms_attendance_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.woridge.ms_attendance_management.dto.ErrorResponse;
import org.woridge.ms_attendance_management.enumeration.ErrorCode;
import org.woridge.ms_attendance_management.exception.absence.AbsenceFinalStateException;
import org.woridge.ms_attendance_management.exception.absence.AbsenceNotFoundException;
import org.woridge.ms_attendance_management.exception.parameter.BusinessParameterException;
import org.woridge.ms_attendance_management.exception.rfid.EmployeeAlreadyHasCardException;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardAlreadyExistsException;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardNotFoundException;
import org.woridge.ms_attendance_management.exception.rfid.ScanTooFastException;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── RFID ─────────────────────────────────────────────────────

    @ExceptionHandler(RfidCardNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleRfidCardNotFoundException(
            RfidCardNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.RFID_CARD_NOT_FOUND
                ));
    }

    @ExceptionHandler(RfidCardAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<String>> handleRfidCardAlreadyExistsException(
            RfidCardAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.RFID_CARD_ALREADY_EXISTS
                ));
    }

    @ExceptionHandler(EmployeeAlreadyHasCardException.class)
    public ResponseEntity<ErrorResponse<String>> handleEmployeeAlreadyHasCardException(
            EmployeeAlreadyHasCardException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.EMPLOYEE_ALREADY_HAS_CARD
                ));
    }

    @ExceptionHandler(ScanTooFastException.class)
    public ResponseEntity<ErrorResponse<String>> handleScanTooFastException(
            ScanTooFastException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.SCAN_TOO_FAST
                ));
    }

    // ─── ABSENCE ──────────────────────────────────────────────────

    @ExceptionHandler(AbsenceNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleAbsenceNotFoundException(AbsenceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.ABSENCE_NOT_FOUND
                ));
    }

    @ExceptionHandler(AbsenceFinalStateException.class)
    public ResponseEntity<ErrorResponse<String>> handleAbsenceFinalStateException(AbsenceFinalStateException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.ABSENCE_FINAL_STATE
                ));
    }

    @ExceptionHandler(BusinessParameterException.class)
    public ResponseEntity<ErrorResponse<String>> handleBusinessParameterException(BusinessParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.BUSINESS_PARAMETER_INVALID
                ));
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleUserNotFoundException(UserNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.USERNOTFOUND
                ));
    }
}