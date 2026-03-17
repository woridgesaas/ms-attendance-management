package org.woridge.ms_attendance_management.mapper;


import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;
import org.woridge.ms_attendance_management.model.RfidCard;
import org.woridge.ms_attendance_management.model.WorkingDay;

import java.time.format.DateTimeFormatter;

@Component
public class RfidCardMapper {

    // Format HH:mm:ss → ex: "08:30:00"
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public ScanResponse toCheckInResponse(RfidCard card, WorkingDay workingDay) {
        return ScanResponse.builder()
                .action("CHECK_IN")
                .employeeName(getFullName(card))
                .checkInTime(workingDay.getCheckInTime().format(TIME_FORMATTER))
                .build();
    }

    public ScanResponse toCheckOutResponse(RfidCard card, WorkingDay workingDay) {
        return ScanResponse.builder()
                .action("CHECK_OUT")
                .employeeName(getFullName(card))
                .checkInTime(workingDay.getCheckInTime().format(TIME_FORMATTER))
                .checkOutTime(workingDay.getCheckOutTime().format(TIME_FORMATTER))
                .build();
    }

    public ScanResponse toEnrolledResponse(RfidCard card) {
        return ScanResponse.builder()
                .action("ENROLLED")
                .employeeName(getFullName(card))
                .build();
    }

    private String getFullName(RfidCard card) {
        return card.getEmployee().getFirstName()
                + " "
                + card.getEmployee().getLastName();
    }
}