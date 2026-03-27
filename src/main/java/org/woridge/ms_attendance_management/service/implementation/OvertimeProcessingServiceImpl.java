package org.woridge.ms_attendance_management.service.implementation;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;
import org.woridge.ms_attendance_management.model.Overtime;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.repository.OvertimeRepository;
import org.woridge.ms_attendance_management.service.OvertimeProcessingService;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Slf4j
@AllArgsConstructor
public class OvertimeProcessingServiceImpl implements OvertimeProcessingService {
    @Autowired
    private OvertimeRepository overtimeRepository;
    @Override
    @Transactional
    public void detectAndCreate(WorkingDay workingDay) {
        LocalDate date = workingDay.getDate();
        LocalTime checkOut = workingDay.getCheckOutTime();
        //No overtime here
        if(checkOut == null || checkOut.isBefore(DEFAULT_END_TIME.plusMinutes(MIN_OVERTIME_MINUTES)))
            return;
        double h25 = 0, h50 = 0, h100 = 0;
        boolean isWeekEnd = (date.getDayOfWeek() == DayOfWeek.SUNDAY);
        if (isWeekEnd) {

            h50 = calculateHoursBetween(DEFAULT_END_TIME, checkOut, DEFAULT_END_TIME, NIGHT_START_TIME);
            h100 = calculateHoursBetween(DEFAULT_END_TIME, checkOut, NIGHT_START_TIME, LocalTime.MAX);
        } else {
            h25 = calculateHoursBetween(DEFAULT_END_TIME, checkOut, DEFAULT_END_TIME, NIGHT_START_TIME);
            h50 = calculateHoursBetween(DEFAULT_END_TIME, checkOut, NIGHT_START_TIME, LocalTime.MAX);
        }
        Overtime overtime = Overtime.builder()
                .workingDay(workingDay)
                .user(workingDay.getPerformedBy())
                .hours25(h25)
                .hours50(h50)
                .hours100(h100)
                .status(OvertimeStatus.PENDING)
                .build();

        overtimeRepository.save(overtime);
    }

    private double calculateHoursBetween(LocalTime startOT, LocalTime endOT, LocalTime limitStart, LocalTime limitEnd) {

        LocalTime actualStart = startOT.isAfter(limitStart) ? startOT : limitStart;
        LocalTime actualEnd = endOT.isBefore(limitEnd) ? endOT : limitEnd;

        if (actualStart.isBefore(actualEnd)) {
            long minutes = Duration.between(actualStart, actualEnd).toMinutes();
            return minutes / 60.0;
        }
        return 0;
    }
}
