package org.woridge.ms_attendance_management.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class UserCreatedEvent {
    @JsonAlias({"Cin", "CIN", "cin"})
    private String cin;
    private String firstName;
    private String lastName;
    private String email;
    private String departmentId;
    private String role;

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "cin='" + cin + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", departmentId='" + departmentId + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}