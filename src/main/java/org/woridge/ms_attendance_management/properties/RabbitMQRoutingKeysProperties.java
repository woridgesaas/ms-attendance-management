package org.woridge.ms_attendance_management.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class RabbitMQRoutingKeysProperties {

    private String absenceAlertRh;
    private String absenceAlertCeo;
    private String holidayCreated;
    private String overtimeProcessedAlert;
    private String leaveProcessed;
    private String tenantReady;
}