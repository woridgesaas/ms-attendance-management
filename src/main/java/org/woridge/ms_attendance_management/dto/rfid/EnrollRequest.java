package org.woridge.ms_attendance_management.dto.rfid;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EnrollRequest {

    // L'UUID de l'employé à qui on veut associer la carte
    private UUID employeeId;
}
