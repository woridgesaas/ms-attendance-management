package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.dto.WorkingDay.*;
import org.woridge.ms_attendance_management.enumeration.AttendanceStatus;
import org.woridge.ms_attendance_management.exception.AttendanceNotFoundException;
import org.woridge.ms_attendance_management.exception.leave.OnlyManagerAccessException;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.mapper.AttendanceMapper;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.model.WorkingDay;
import org.woridge.ms_attendance_management.properties.BusinessProperties;
import org.woridge.ms_attendance_management.repository.RfidCardRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.repository.WorkingDayRepository;
import org.woridge.ms_attendance_management.service.AttendanceService;
import org.woridge.ms_attendance_management.service.policy.AttendancePolicyService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for orchestrating attendance operations.
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final WorkingDayRepository WorkingDayRepository;
    private final AttendanceMapper AttendanceMapper;
    private final AttendancePolicyService AttendancePolicyService;
    private final BusinessProperties businessProperties;

    private final UserRepository userRepository;
    private final RfidCardRepository rfidCardRepository;

    // ===============================
    // PUBLIC METHODS (existants)
    // ===============================

    @Override
    public List<AttendanceResponse> getUserAttendance(UUID userId) {
        return WorkingDayRepository.findByPerformedBy_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public double calculateWeekHours(UUID userId) {
        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = LocalDate.now().with(DayOfWeek.SUNDAY);
        return calculatePeriod(userId, start, end);
    }

    @Override
    public double calculateMonthHours(UUID userId) {
        YearMonth month = YearMonth.now();
        return calculatePeriod(
                userId,
                month.atDay(1),
                month.atEndOfMonth()
        );
    }

    @Override
    public double calculateTodayHours(UUID userId) {
        WorkingDay day = getTodayAttendance(userId);
        return workedHours(day);
    }

    @Override
    public AttendanceChartResponse getTodayChart(UUID userId) {
        WorkingDay day = getTodayAttendance(userId);
        double worked = workedHours(day);
        double productive = AttendancePolicyService.calculateProductiveHours(day.getDate(), worked);
        double overtime = AttendancePolicyService.calculateOvertime(day.getDate(), worked);
        return AttendanceMapper.toChartResponse(worked, productive, overtime);
    }

    @Override
    public AttendanceDashboardResponse getDashboard(UUID userId) {
        double today = calculateTodayHours(userId);
        double week = calculateWeekHours(userId);
        double month = calculateMonthHours(userId);

        double overtime = WorkingDayRepository.findByPerformedBy_Id(userId)
                .stream()
                .mapToDouble(day -> AttendancePolicyService.calculateOvertime(
                        day.getDate(),
                        workedHours(day)
                ))
                .sum();

        return AttendanceDashboardResponse.builder()
                .today(new HoursWithLimit(today, businessProperties.getDailyMaxHours()))
                .week(new HoursWithLimit(week, businessProperties.getWeeklyMaxHours()))
                .month(new HoursWithLimit(month, businessProperties.getMonthlyMaxHours()))
                .overtimeHours(new HoursWithLimit(overtime, businessProperties.getOvertimeMaxHours()))
                .build();
    }

    // ===============================
    // NOUVELLE MÉTHODE : LISTE ATTENDANCE
    // ===============================

    @Override
    public List<AttendanceListResponse> getAttendanceList(
            String userCin, String role,
            LocalDate startDate, LocalDate endDate,
            UUID departmentIdFilter) {

        List<WorkingDay> workingDays;

        // 1. RH voit tout (ou filtre par département s'il le demande)
        if ("HR".equals(role) || "RH".equals(role)) {
            if (departmentIdFilter != null) {
                workingDays = WorkingDayRepository.findByDepartmentAndDateRange(
                        departmentIdFilter, startDate, endDate);
            } else {
                workingDays = WorkingDayRepository.findAllByDateRange(startDate, endDate);
            }
        }
        // 2. MANAGER voit seulement son département
        else if ("MANAGER".equals(role)) {
            User manager = userRepository.findByCin(userCin)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));

            workingDays = WorkingDayRepository.findByDepartmentAndDateRange(
                    manager.getDepartmentId(), startDate, endDate);
        }
        // 3. Autres rôles interdits
        else {
            throw new OnlyManagerAccessException(
                    "Only managers or HR can view the attendance list");
        }

        // 4. Transformer en DTO
        return workingDays.stream()
                .map(this::toAttendanceListResponse)
                .toList();
    }

    // ===============================
    // PRIVATE HELPERS
    // ===============================

    private double calculatePeriod(UUID userId, LocalDate start, LocalDate end) {
        return WorkingDayRepository.findBetweenDates(userId, start, end)
                .stream()
                .mapToDouble(this::workedHours)
                .sum();
    }

    private WorkingDay getTodayAttendance(UUID userId) {
        return WorkingDayRepository.findByPerformedBy_IdAndDate(userId, LocalDate.now())
                .orElseThrow(() ->
                        new AttendanceNotFoundException("No attendance found for today"));
    }

    private AttendanceResponse mapToResponse(WorkingDay day) {
        double worked = workedHours(day);
        return AttendanceMapper.toResponse(
                day,
                AttendancePolicyService.calculateStatus(
                        day.getDate(),
                        day.getCheckInTime(),
                        day.getCheckOutTime()
                ),
                worked
        );
    }

    private double workedHours(WorkingDay day) {
        return AttendancePolicyService.calculateWorkedHours(
                day.getCheckInTime(),
                day.getCheckOutTime()
        );
    }

    // ── Helper : convertir WorkingDay en AttendanceListResponse ──
    private AttendanceListResponse toAttendanceListResponse(WorkingDay wd) {
        User employee = wd.getPerformedBy();

        // Calcul des heures de production (check_out - check_in)
        double productionHours = 0.0;
        if (wd.getCheckInTime() != null && wd.getCheckOutTime() != null) {
            long minutes = java.time.Duration.between(
                    wd.getCheckInTime(), wd.getCheckOutTime()).toMinutes();
            productionHours = minutes / 60.0;
        }

        // Statut calculé
        AttendanceStatus status = calculateAttendanceListStatus(wd, productionHours);

        // Overtime infos
        UUID overtimeId = wd.getOvertime() != null ? wd.getOvertime().getId() : null;
        String overtimeStatus = wd.getOvertime() != null
                ? wd.getOvertime().getStatus().name()
                : null;

        // Carte RFID ?
        boolean hasRfidCard = rfidCardRepository.existsByEmployeeIdAndActiveTrue(employee.getId());

        return AttendanceListResponse.builder()
                .workingDayId(wd.getId())
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .departmentId(employee.getDepartmentId())
                .departmentName(employee.getDepartmentId() != null
                        ? "Dpt " + employee.getDepartmentId().toString().substring(0, 8)
                        : "N/A")
                .date(wd.getDate())
                .checkInTime(wd.getCheckInTime())
                .checkOutTime(wd.getCheckOutTime())
                .productionHours(Math.round(productionHours * 100.0) / 100.0)
                .status(status)
                .overtimeId(overtimeId)
                .overtimeStatus(overtimeStatus)
                .hasRfidCard(hasRfidCard)
                .build();
    }

    private AttendanceStatus calculateAttendanceListStatus(WorkingDay wd, double hours) {
        if (wd.getCheckInTime() == null) return AttendanceStatus.ABSENT;
        if (wd.getOvertime() != null) return AttendanceStatus.OVERTIME;
        if (hours < 4) return AttendanceStatus.HALF_DAY;
        if (hours < 7) return AttendanceStatus.UNDER_TIME;
        return AttendanceStatus.PRESENT;
    }
}