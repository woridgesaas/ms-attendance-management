package org.woridge.ms_attendance_management.dto.leave;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestAdminResponse {

    private UUID id;
    private String employeeName;
    private String employeeRole;

    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;

    private String status;

    private String approvedBy;
}