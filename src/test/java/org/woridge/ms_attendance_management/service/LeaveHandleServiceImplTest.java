package org.woridge.ms_attendance_management.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.woridge.ms_attendance_management.enumeration.LeaveType;
import org.woridge.ms_attendance_management.exception.leave.InsufficientLeaveBalanceException;
import org.woridge.ms_attendance_management.exception.leave.JustificationRequiredException;
import org.woridge.ms_attendance_management.exception.leave.LeaveRequestException;
import org.woridge.ms_attendance_management.model.*;
import org.woridge.ms_attendance_management.repository.*;
import org.woridge.ms_attendance_management.service.implementation.LeaveServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LeaveHandleServiceImplTest {
    @Mock
    private   LeaveRequestRepository leaveRequestRepository;
    @Mock
    private  HolidayRepository holidayRepository;
    @Mock
    private   BalanceRepository balanceRepository;
    @Mock
    private  UserRepository userRepository;
    @Mock
    private   ValidationRepository validationRepository;
    @Mock
    private LeaveDayRepository leaveDayRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private UUID randomLeaveId;
    @BeforeEach
    public void setUp(){
        this.randomLeaveId = UUID.randomUUID();
    }
    @Test
    public void shouldApproveLeaveAndDeductBalance(){
        UUID requestId = UUID.randomUUID();
        User employee = User.builder().id(UUID.randomUUID()).cin("EE123").build();

        LeaveRequest request = LeaveRequest.builder()
                .id(requestId)
                .user(employee)
                .startDate(LocalDate.of(2026, 4, 6))
                .endDate(LocalDate.of(2026, 4, 7))
                .leaveType(LeaveType.ANNUEL)
                .createdAt(LocalDateTime.now())
                .build();

        Balance balance = Balance.builder()
                .remainingBalance(20)
                .userId(employee.getId())
                .build();


        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(holidayRepository.existsByDate(any())).thenReturn(false);
        when(balanceRepository.findByUserIdAndYear(any(), anyInt())).thenReturn(Optional.of(balance));
        when(userRepository.findByCin(anyString())).thenReturn(Optional.of(new User()));

        this.leaveService.approveLeave(requestId , "FH123");

        assertEquals(18, balance.getRemainingBalance());
        assertEquals(2 , request.getLeaveDays().size());
        verify(validationRepository, times(1)).save(any(Validation.class));
    }

    @Test
    public void shouldThrowLeaveRequestNotFoundException(){
        when(this.leaveRequestRepository.findById(any()))
                .thenReturn(Optional.empty());

        Exception message =assertThrows(LeaveRequestException.class ,
                ()-> this.leaveService.approveLeave(this.randomLeaveId , "FH123")
                );
        assertEquals("Leave Request not found" , message.getMessage() );
    }
    @Test
    public void shouldThrowInsufficientBalanceException(){
        Balance balance = new Balance();
        balance.setRemainingBalance(0);

        LeaveRequest request = new LeaveRequest();
        request.setLeaveType(LeaveType.MALADIE);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setCreatedAt(LocalDateTime.now());
        request.setJustification(new Justification());
        request.setUser(new User());
        when(leaveRequestRepository.findById(any()))
                .thenReturn(Optional.of(request));
        when(balanceRepository.findByUserIdAndYear(any(), anyInt()))
                .thenReturn(Optional.of(balance));

        assertThrows(InsufficientLeaveBalanceException.class,
                () -> leaveService.approveLeave(this.randomLeaveId, "managerCin"));
    }
    @Test
    public void shouldThrowJustificationNotFoundException(){
        Balance balance = new Balance();
        balance.setRemainingBalance(0);

        LeaveRequest request = new LeaveRequest();
        request.setLeaveType(LeaveType.MALADIE);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setCreatedAt(LocalDateTime.now());
        when(leaveRequestRepository.findById(any()))
                .thenReturn(Optional.of(request));
        Exception ex= assertThrows(JustificationRequiredException.class,
                () -> leaveService.approveLeave(this.randomLeaveId, "managerCin"));
        assertEquals("No justification found" , ex.getMessage());
    }

    @Test
    @DisplayName("Should not deduct days or create LeaveDays when request is only on weekends")
    public void ShouldNotDeductDaysOnWeekend() {
        UUID requestId = UUID.randomUUID();
        String managerCin = "M123";
        LocalDate saturday = LocalDate.of(2026, 4, 11);
        LocalDate sunday = LocalDate.of(2026, 4, 12);

        User employee = User.builder().id(UUID.randomUUID()).build();


        LeaveRequest request = LeaveRequest.builder()
                .id(requestId)
                .user(employee)
                .startDate(saturday)
                .endDate(sunday)
                .leaveType(LeaveType.ANNUEL)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        Balance initialBalance = Balance.builder()
                .userId(employee.getId())
                .year(2026)
                .remainingBalance(22)
                .build();


        when(leaveRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(balanceRepository.findByUserIdAndYear(employee.getId(), 2026))
                .thenReturn(Optional.of(initialBalance));
        when(userRepository.findByCin(managerCin))
                .thenReturn(Optional.of(User.builder().cin(managerCin).build()));

        //ACT
        this.leaveService.approveLeave(requestId, managerCin);

        //ASSERT
        assertEquals(22, initialBalance.getRemainingBalance(), "Balance should remain unchanged for weekend-only requests");
        verify(this.validationRepository, times(1)).save(any(Validation.class));
    }

    @Test
    @DisplayName("Should not deduct days when a leave day is a holiday")
    public void shouldNotDeductDaysOnHoliday(){
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        LocalDate holidayDate = LocalDate.of(2026, 4, 6); // lundi

        LeaveRequest request = new LeaveRequest();
        request.setStartDate(holidayDate);
        request.setEndDate(holidayDate);
        request.setLeaveType(LeaveType.ANNUEL);
        request.setCreatedAt(LocalDateTime.now());
        request.setUser(user);

        Balance balance = new Balance();
        balance.setRemainingBalance(10);

        when(leaveRequestRepository.findById(any()))
                .thenReturn(Optional.of(request));
        when(holidayRepository.existsByDate(holidayDate))
                .thenReturn(true); // jour férié
        when(balanceRepository.findByUserIdAndYear(eq(userId), anyInt()))
                .thenReturn(Optional.of(balance));
        when(userRepository.findByCin(any()))
                .thenReturn(Optional.of(new User(UUID.randomUUID())));

        // Act
        leaveService.approveLeave(UUID.randomUUID(), "managerCin");

        // Assert
        assertEquals(10, balance.getRemainingBalance());
    }

}
