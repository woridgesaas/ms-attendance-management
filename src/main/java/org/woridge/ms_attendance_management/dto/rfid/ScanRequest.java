package org.woridge.ms_attendance_management.dto.rfid;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanRequest {

    // L'UID physique de la carte ex: "A3:F2:11:BC"
    private String uid;

    // Identifiant de l'ESP8266 ex: "ESP-ENTREE-01"
    // utile si tu as plusieurs lecteurs dans l'entreprise
    private String deviceId;
}