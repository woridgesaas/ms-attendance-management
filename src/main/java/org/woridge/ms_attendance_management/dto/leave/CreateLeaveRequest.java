package org.woridge.ms_attendance_management.dto.leave;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.woridge.ms_attendance_management.enumeration.LeaveType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeaveRequest {

    @NotNull(message = "Le type de congé est obligatoire")
    private LeaveType leaveType;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;
}
