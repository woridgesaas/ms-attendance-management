package org.woridge.ms_attendance_management.dto.WorkingDay;

import lombok.*;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceListResponse {

    private UUID workingDayId;

    // Infos employé
    private UUID employeeId;
    private String employeeName;
    private String departmentName;  // temporaire : sera "Dpt xxx"
    private UUID departmentId;

    // Données du jour
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    // Calculs
    private Double productionHours;
    private AttendanceStatus status;

    // Overtime lié
    private UUID overtimeId;          // null si pas d'overtime
    private String overtimeStatus;    // PENDING / APPROVED / REJECTED

    // Carte RFID
    private Boolean hasRfidCard;
}