package org.woridge.ms_attendance_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.woridge.ms_attendance_management.dto.ErrorResponse;
import org.woridge.ms_attendance_management.enumeration.ErrorCode;
import org.woridge.ms_attendance_management.exception.absence.AbsenceFinalStateException;
import org.woridge.ms_attendance_management.exception.absence.AbsenceNotFoundException;
import org.woridge.ms_attendance_management.exception.leave.*;
import org.woridge.ms_attendance_management.exception.overtime.OvertimeAlreadyProcessedException;
import org.woridge.ms_attendance_management.exception.overtime.OvertimeNotFoundException;
import org.woridge.ms_attendance_management.exception.parameter.BusinessParameterException;
import org.woridge.ms_attendance_management.exception.rfid.*;
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

    @ExceptionHandler(DayAlreadyCompleteException.class)
    public ResponseEntity<ErrorResponse<String>> handleDayAlreadyCompleteException(
            DayAlreadyCompleteException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.createErrorResponse(
                        ex.getMessage(),
                        ErrorCode.DAY_ALREADY_COMPLETE
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
    @ExceptionHandler(OvertimeAlreadyProcessedException.class)
    public ResponseEntity<ErrorResponse<String>> handlleOvertimeAlreadyProcessed(OvertimeAlreadyProcessedException exception){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.OVERTIME_ALREADY_PROCESSED
                ));
    }
    @ExceptionHandler(OvertimeNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handlleOvertimeNotFound(OvertimeNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.OVERTIME_NOT_FOUND
                ));

    }
    @ExceptionHandler(JustificationRequiredException.class)
    public ResponseEntity<ErrorResponse> handleJustification(JustificationRequiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.createErrorResponse(ex.getMessage(), ErrorCode.JUSTIFICATION_REQUIRED));
    }

    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<ErrorResponse> handleBalance(InsufficientLeaveBalanceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.createErrorResponse(ex.getMessage(),ErrorCode.INSUFFICIENT_BALANCE));
    }

    @ExceptionHandler(InvalidLeaveDatesException.class)
    public ResponseEntity<ErrorResponse> handleDates(InvalidLeaveDatesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.createErrorResponse(ex.getMessage(),ErrorCode.INVALID_DATES));
    }
    @ExceptionHandler(OnlyManagerAccessException.class)
    public ResponseEntity<ErrorResponse> handleOnlyManager(OnlyManagerAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.createErrorResponse(ex.getMessage(), ErrorCode.ACCESS_DENIED));
    }

    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleLeaveRequestNotFoundException(LeaveRequestNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.createErrorResponse(
                        exception.getMessage(),
                        ErrorCode.LEAVES_NOT_FOUND
                ));
    }
}