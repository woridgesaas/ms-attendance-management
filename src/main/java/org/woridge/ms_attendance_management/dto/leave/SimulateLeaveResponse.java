package org.woridge.ms_attendance_management.dto.leave;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulateLeaveResponse {
    private int numberOfDays;
    private int remainingBalance;
}
