package org.woridge.ms_attendance_management.dto.leave;

import lombok.*;
import org.woridge.ms_attendance_management.enumeration.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponse {

    private UUID id;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private int remainingBalance;
    private String justificationPath;
    private LocalDateTime requestDate;
}
