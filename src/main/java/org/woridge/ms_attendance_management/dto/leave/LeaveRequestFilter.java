package org.woridge.ms_attendance_management.dto.leave;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.woridge.ms_attendance_management.enumeration.LeaveType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestFilter {

    private LeaveType leaveType;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
