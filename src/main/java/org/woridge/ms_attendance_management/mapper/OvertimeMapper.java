package org.woridge.ms_attendance_management.mapper;

import org.woridge.ms_attendance_management.dto.overtime.OvertimeIndexResponse;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;
import org.woridge.ms_attendance_management.messaging.event.OvertimeHandleEvent;
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

    //Use to dispatch event:
    public static OvertimeHandleEvent toOvertimeAlertEvent(Overtime model ,String employeCin, String employeEmail){
        return OvertimeHandleEvent.builder().
                employeeFullName(model.getUser().getFullName()).
                employeeCin(employeCin)
                .employeeEmail(employeEmail)
                .status(model.getStatus())
                .managerComment(model.getManagerComment())
                .processedAt(model.getUpdatedAt())
                .totalHours(model.getTotal())
                .workingDay(model.getWorkingDay().getDate())
                .build();
    }
}
