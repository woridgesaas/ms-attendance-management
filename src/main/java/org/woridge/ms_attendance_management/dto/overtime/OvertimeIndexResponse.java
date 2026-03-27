package org.woridge.ms_attendance_management.dto.overtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OvertimeIndexResponse {
    private UUID id;
    private double totalHours;
    private OvertimeStatus status;

}
