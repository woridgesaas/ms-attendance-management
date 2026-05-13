package org.woridge.ms_attendance_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.dto.leave.CreateLeaveRequest;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestResponse;
import org.woridge.ms_attendance_management.enumeration.LeaveType;
import org.woridge.ms_attendance_management.exception.leave.InsufficientLeaveBalanceException;
import org.woridge.ms_attendance_management.exception.leave.InvalidLeaveDatesException;
import org.woridge.ms_attendance_management.exception.leave.JustificationRequiredException;
import org.woridge.ms_attendance_management.exception.leave.LeaveRequestException;
import org.woridge.ms_attendance_management.mapper.LeaveRequestMapper;
import org.woridge.ms_attendance_management.model.Balance;
import org.woridge.ms_attendance_management.model.LeaveRequest;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.repository.BalanceRepository;
import org.woridge.ms_attendance_management.repository.JustificationRepository;
import org.woridge.ms_attendance_management.repository.LeaveRequestRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.implementation.LeaveRequestServiceImpl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock private LeaveRequestRepository  leaveRequestRepository;
    @Mock private BalanceRepository       balanceRepository;
    @Mock private JustificationRepository justificationRepository;
    @Mock private MinioStorageService     minioStorageService;
    @Mock private UserRepository          userRepository;
    @Mock private LeaveRequestMapper leaveRequestMapper;  // ← ajouté

    @InjectMocks
    private LeaveRequestServiceImpl service;

    private final String USER_CIN = "F123456";
    private final UUID   USER_ID  = UUID.randomUUID();
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(USER_ID)
                .cin(USER_CIN)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(userRepository.findByCin(USER_CIN))
                .thenReturn(Optional.of(mockUser));
    }

    @Test
    void createAnnualLeave_noFile_success() {
        CreateLeaveRequest dto = buildDto(LeaveType.ANNUEL,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        mockMapperToDto("annuel_data", dto);
        mockBalance(10);
        mockSave(dto);
        mockMapperToResponse();

        LeaveRequestResponse result = service.createLeaveRequest(USER_CIN, "annuel_data", null);

        assertThat(result).isNotNull();
        assertThat(result.getLeaveType()).isEqualTo(LeaveType.ANNUEL);
        verify(minioStorageService, never()).uploadJustification(any(), any());
    }


    @Test
    void createMedicalLeave_noFile_throwsJustificationRequired() {
        CreateLeaveRequest dto = buildDto(LeaveType.MALADIE,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        mockMapperToDto("maladie_data", dto);

        assertThatThrownBy(() -> service.createLeaveRequest(USER_CIN, "maladie_data", null))
                .isInstanceOf(JustificationRequiredException.class);
    }


    @Test
    void createExceptionalLeave_withFile_success() {
        CreateLeaveRequest dto = buildDto(LeaveType.EXCEPTIONNEL,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));

        MultipartFile file = mockFile();
        mockMapperToDto("exceptionnel_data", dto);
        mockBalance(10);
        mockSave(dto);
        mockMapperToResponse();

        when(minioStorageService.uploadJustification(any(), any()))
                .thenReturn("justifications/xxx/file.pdf");

        LeaveRequestResponse result = service.createLeaveRequest(USER_CIN, "exceptionnel_data", file);

        assertThat(result.getJustificationPath()).isNotBlank();
        verify(justificationRepository).save(any());
    }


    @Test
    void createLeave_insufficientBalance_throwsException() {
        CreateLeaveRequest dto = buildDto(LeaveType.ANNUEL,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(14));

        mockMapperToDto("balance_data", dto);
        mockBalance(3); // solde insuffisant

        assertThatThrownBy(() -> service.createLeaveRequest(USER_CIN, "balance_data", null))
                .isInstanceOf(InsufficientLeaveBalanceException.class)
                .hasMessageContaining("votre solde est insuffisant");
    }


    @Test
    void createLeave_invalidDates_throwsException() {
        CreateLeaveRequest dto = buildDto(LeaveType.ANNUEL,
                LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 5));

        mockMapperToDto("dates_data", dto);

        assertThatThrownBy(() -> service.createLeaveRequest(USER_CIN, "dates_data", null))
                .isInstanceOf(InvalidLeaveDatesException.class);
    }

    @Test
    void createLeave_userNotFound_throwsException() {
        CreateLeaveRequest dto = buildDto(LeaveType.ANNUEL,
                LocalDate.of(2024, 6, 3), LocalDate.of(2024, 6, 5));

        mockMapperToDto("user_data", dto);
        when(userRepository.findByCin(USER_CIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLeaveRequest(USER_CIN, "user_data", null))
                .isInstanceOf(LeaveRequestException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    // --- Helpers ---

    private CreateLeaveRequest buildDto(LeaveType type, LocalDate start, LocalDate end) {
        return CreateLeaveRequest.builder()
                .leaveType(type).startDate(start).endDate(end).build();
    }

    private void mockMapperToDto(String data, CreateLeaveRequest dto) {
        when(leaveRequestMapper.toCreateLeaveRequest(data)).thenReturn(dto);
    }

    private void mockBalance(int remaining) {
        Balance balance = Balance.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .year(LocalDate.now().getYear())
                .initialBalance(25)
                .remainingBalance(remaining)
                .build();

        when(balanceRepository.findByUserIdAndYear(USER_ID, LocalDate.now().getYear()))
                .thenReturn(Optional.of(balance));
    }

    private void mockSave(CreateLeaveRequest dto) {
        LeaveRequest lr = LeaveRequest.builder()
                .id(UUID.randomUUID())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .leaveType(dto.getLeaveType())
                .build();

        when(leaveRequestMapper.toLeaveRequest(any(), any())).thenReturn(lr);
        when(leaveRequestRepository.save(any())).thenReturn(lr);
    }

    private void mockMapperToResponse() {
        when(leaveRequestMapper.toLeaveRequestResponse(any(), anyInt(), anyInt(), any()))
                .thenAnswer(inv -> {
                    LeaveRequest lr = inv.getArgument(0);
                    return LeaveRequestResponse.builder()
                            .id(lr.getId())
                            .leaveType(lr.getLeaveType())
                            .startDate(lr.getStartDate())
                            .endDate(lr.getEndDate())
                            .numberOfDays(inv.getArgument(1))
                            .remainingBalance(inv.getArgument(2))
                            .justificationPath(inv.getArgument(3))
                            .build();
                });
    }

    private MultipartFile mockFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        return file;
    }
}