package org.woridge.ms_attendance_management.mapper;

import org.woridge.ms_attendance_management.dto.overtime.OvertimeIndexResponse;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;
import org.woridge.ms_attendance_management.model.Overtime;

import java.util.UUID;

public  class OvertimeMapper {
    public static OvertimeIndexResponse toIndexResponse(Overtime model){
        return OvertimeIndexResponse.builder()
                .id(model.getId())
                .totalHours(model.getTotal())
                .status(model.getStatus())
                .build();
    }
}
