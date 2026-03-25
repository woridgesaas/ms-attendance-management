package org.woridge.ms_attendance_management.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "attendance.business")
@Getter
@Setter
public class BusinessProperties {

    private int justificationDeadlineDays = 3;
    private int rhAlertThreshold = 2;
    private int ceoAlertThreshold = 3;

    private double dailyMaxHours = 9.0;
    private double weeklyMaxHours = 40.0;
    private double monthlyMaxHours = 98.0;
    private double overtimeMaxHours = 28.0;
}
