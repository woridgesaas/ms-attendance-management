package org.woridge.ms_attendance_management.dto.leave;

import lombok.*;
import org.woridge.ms_attendance_management.enumeration.LeaveType;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarLeaveResponse {

    // Identifiant unique du congé (utile pour clic → détail)
    private UUID leaveId;

    // Infos employé
    private UUID employeeId;
    private String employeeName;

    // Département — utile pour RH qui voit plusieurs départements
    private UUID departmentId;

    // Période du congé
    private LocalDate startDate;
    private LocalDate endDate;

    // Type (pour couleur dans le calendrier)
    private LeaveType leaveType;
}