package org.woridge.ms_attendance_management.dto.rfid;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScanResponse {

    // CHECK_IN, CHECK_OUT, ENROLLED, UNKNOWN
    private String action;

    private String employeeName;

    // Heure d'entrée ex: "08:30:00"
    private String checkInTime;

    // Heure de sortie ex: "17:00:00"
    private String checkOutTime;
}