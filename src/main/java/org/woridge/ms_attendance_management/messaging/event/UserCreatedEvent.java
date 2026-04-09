package org.woridge.ms_attendance_management.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class UserCreatedEvent {
    private String cin;
    private String firstName;
    private String lastName;
    private String email;
    private String departmentId;
    private String role;
}