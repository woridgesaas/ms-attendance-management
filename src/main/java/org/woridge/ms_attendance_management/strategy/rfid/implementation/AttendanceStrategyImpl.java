package org.woridge.ms_attendance_management.strategy.rfid.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.rfid.ScanRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;
import org.woridge.ms_attendance_management.model.RfidCard;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardNotFoundException;
import org.woridge.ms_attendance_management.exception.rfid.ScanTooFastException;
import org.woridge.ms_attendance_management.mapper.RfidCardMapper;
import org.woridge.ms_attendance_management.repository.RfidCardRepository;
import org.woridge.ms_attendance_management.repository.WorkingDayRepository;
import org.woridge.ms_attendance_management.strategy.rfid.ScanStrategy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttendanceStrategyImpl implements ScanStrategy {

    private final RfidCardRepository rfidCardRepository;
    private final WorkingDayRepository workingDayRepository;
    private final RfidCardMapper rfidCardMapper;

    private static final int MIN_DELAY_MINUTES = 2;

    @Override
    public ScanResponse handle(ScanRequest request) {

        // 1. Carte connue et active ?
        RfidCard card = rfidCardRepository
                .findByUidAndActiveTrue(request.getUid())
                .orElseThrow(() -> new RfidCardNotFoundException(request.getUid()));

        // 2. Pointage existant aujourd'hui ?
        Optional<WorkingDay> existingDay = workingDayRepository
                .findByPerformedByIdAndDate(
                        card.getEmployee().getId(),
                        LocalDate.now()
                );

        if (existingDay.isPresent()) {
            WorkingDay workingDay = existingDay.get();

            // 3. Anti-double-scan
            LocalTime lastAction = workingDay.getCheckOutTime() != null
                    ? workingDay.getCheckOutTime()
                    : workingDay.getCheckInTime();

            long minutesSinceLast = Duration.between(
                    lastAction, LocalTime.now()
            ).toMinutes();

            if (minutesSinceLast < MIN_DELAY_MINUTES) {
                throw new ScanTooFastException();
            }

            // 4. CHECK_OUT
            if (workingDay.getCheckOutTime() == null) {
                workingDay.setCheckOutTime(LocalTime.now());
                workingDayRepository.save(workingDay);
                return rfidCardMapper.toCheckOutResponse(card, workingDay);
            }
        }

        // 5. CHECK_IN
        WorkingDay newDay = new WorkingDay();
        newDay.setDate(LocalDate.now());
        newDay.setCheckInTime(LocalTime.now());
        newDay.setPerformedBy(card.getEmployee());
        workingDayRepository.save(newDay);

        return rfidCardMapper.toCheckInResponse(card, newDay);
    }
}