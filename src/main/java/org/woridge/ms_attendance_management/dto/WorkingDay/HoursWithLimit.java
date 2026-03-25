package org.woridge.ms_attendance_management.dto.WorkingDay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoursWithLimit {

    private double value;
    private double max;
}