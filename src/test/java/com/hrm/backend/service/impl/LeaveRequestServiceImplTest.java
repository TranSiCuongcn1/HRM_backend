package com.hrm.backend.service.impl;

import com.hrm.backend.dto.LeaveRequestDTO;
import com.hrm.backend.dto.LeaveRequestResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.LeaveBalance;
import com.hrm.backend.entity.LeaveRequest;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.LeaveBalanceRepository;
import com.hrm.backend.repository.LeaveRequestRepository;
import com.hrm.backend.repository.LeaveTypeRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.EmailService;
import com.hrm.backend.service.LeaveBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaveBalanceService leaveBalanceService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private Employee employee;
    private Employee admin;
    private User employeeUser;
    private User adminUser;
    private LeaveType annualLeave;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .email("employee@example.com")
                .build();
        admin = Employee.builder()
                .id(2)
                .code("ADM0001")
                .name("Admin User")
                .email("admin@example.com")
                .build();
        employeeUser = User.builder()
                .id(1)
                .username("employee")
                .employee(employee)
                .build();
        adminUser = User.builder()
                .id(2)
                .username("admin")
                .employee(admin)
                .build();
        annualLeave = LeaveType.builder()
                .id(1)
                .code("ANNUAL")
                .name("Annual Leave")
                .isPaid(true)
                .build();
    }

    @Test
    @DisplayName("Unit submitRequest - Valid annual leave should create PENDING request")
    void submitRequest_ValidAnnualLeave_CreatesPendingRequest() {
        LeaveRequestDTO request = standardRequest();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveTypeRepository.findById(1)).thenReturn(Optional.of(annualLeave));
        when(leaveRequestRepository.countOverlappingRequests(1, request.getStartDate(), request.getEndDate()))
                .thenReturn(0L);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026))
                .thenReturn(Optional.of(balance(new BigDecimal("12.0"), BigDecimal.ZERO, BigDecimal.ZERO)));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        LeaveRequestResponse response = leaveRequestService.submitRequest("employee", request);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getDays()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(response.getEmployeeCode()).isEqualTo("EMP0001");

        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
        assertThat(captor.getValue().getLeaveType()).isEqualTo(annualLeave);
    }

    @Test
    @DisplayName("Unit submitRequest - End date before start date should throw IllegalArgumentException")
    void submitRequest_EndDateBeforeStartDate_ThrowsException() {
        LeaveRequestDTO request = standardRequest();
        request.setStartDate(LocalDate.of(2026, 6, 5));
        request.setEndDate(LocalDate.of(2026, 6, 4));
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveTypeRepository.findById(1)).thenReturn(Optional.of(annualLeave));

        assertThatThrownBy(() -> leaveRequestService.submitRequest("employee", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kết thúc");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Unit submitRequest - Overlapping request should throw IllegalArgumentException")
    void submitRequest_OverlappingRequest_ThrowsException() {
        LeaveRequestDTO request = standardRequest();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveTypeRepository.findById(1)).thenReturn(Optional.of(annualLeave));
        when(leaveRequestRepository.countOverlappingRequests(1, request.getStartDate(), request.getEndDate()))
                .thenReturn(1L);

        assertThatThrownBy(() -> leaveRequestService.submitRequest("employee", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING/APPROVED");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Unit submitRequest - Weekend single-day leave should throw IllegalArgumentException")
    void submitRequest_WeekendSingleDay_ThrowsException() {
        LeaveRequestDTO request = standardRequest();
        request.setStartDate(LocalDate.of(2026, 6, 6));
        request.setEndDate(LocalDate.of(2026, 6, 6));
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveTypeRepository.findById(1)).thenReturn(Optional.of(annualLeave));
        when(leaveRequestRepository.countOverlappingRequests(1, request.getStartDate(), request.getEndDate()))
                .thenReturn(0L);

        assertThatThrownBy(() -> leaveRequestService.submitRequest("employee", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cuối tuần");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Unit submitRequest - Insufficient balance should throw IllegalArgumentException")
    void submitRequest_InsufficientBalance_ThrowsException() {
        LeaveRequestDTO request = standardRequest();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveTypeRepository.findById(1)).thenReturn(Optional.of(annualLeave));
        when(leaveRequestRepository.countOverlappingRequests(1, request.getStartDate(), request.getEndDate()))
                .thenReturn(0L);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026))
                .thenReturn(Optional.of(balance(new BigDecimal("1.0"), new BigDecimal("1.0"), BigDecimal.ZERO)));

        assertThatThrownBy(() -> leaveRequestService.submitRequest("employee", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không đủ số dư phép");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Unit submitRequest - UNPAID leave should skip balance check")
    void submitRequest_UnpaidLeave_SkipsBalanceCheck() {
        LeaveType unpaid = LeaveType.builder()
                .id(2)
                .code("UNPAID")
                .name("Unpaid Leave")
                .isPaid(false)
                .build();
        LeaveRequestDTO request = standardRequest();
        request.setLeaveTypeId(2);
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveTypeRepository.findById(2)).thenReturn(Optional.of(unpaid));
        when(leaveRequestRepository.countOverlappingRequests(1, request.getStartDate(), request.getEndDate()))
                .thenReturn(0L);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.submitRequest("employee", request);

        assertThat(response.getLeaveTypeCode()).isEqualTo("UNPAID");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(leaveBalanceRepository, never()).findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Unit approveRequest - Pending request should approve and deduct leave balance")
    void approveRequest_PendingRequest_DeductsBalance() {
        LeaveRequest pending = pendingRequest();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.approveRequest("admin", 1);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getApprovedByName()).isEqualTo("Admin User");
        assertThat(response.getApprovedAt()).isNotNull();
        verify(leaveBalanceService).deductBalance(1, 1, 2026, new BigDecimal("1.0"));
    }

    @Test
    @DisplayName("Unit approveRequest - Non-pending request should throw IllegalArgumentException")
    void approveRequest_NonPendingRequest_ThrowsException() {
        LeaveRequest approved = pendingRequest();
        approved.setStatus("APPROVED");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> leaveRequestService.approveRequest("admin", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");

        verify(leaveBalanceService, never()).deductBalance(any(), any(), any(Integer.class), any());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Unit rejectRequest - Pending request should reject without deducting balance")
    void rejectRequest_PendingRequest_DoesNotDeductBalance() {
        LeaveRequest pending = pendingRequest();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.rejectRequest("admin", 1, "Insufficient evidence");

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getRejectionReason()).isEqualTo("Insufficient evidence");
        verify(leaveBalanceService, never()).deductBalance(any(), any(), any(Integer.class), any());
    }

    @Test
    @DisplayName("Unit cancelRequest - Owner can cancel pending request")
    void cancelRequest_OwnerPendingRequest_CancelsRequest() {
        LeaveRequest pending = pendingRequest();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.cancelRequest("employee", 1);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("Unit cancelRequest - Non-owner cannot cancel request")
    void cancelRequest_NonOwner_ThrowsException() {
        LeaveRequest pending = pendingRequest();
        pending.setEmployee(Employee.builder().id(99).code("EMP0099").name("Other").build());
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> leaveRequestService.cancelRequest("employee", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không có quyền");

        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    private LeaveRequestDTO standardRequest() {
        return new LeaveRequestDTO(
                1,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1),
                new BigDecimal("1.0"),
                null,
                "Family matter",
                null
        );
    }

    private LeaveBalance balance(BigDecimal totalDays, BigDecimal usedDays, BigDecimal carryOverDays) {
        return LeaveBalance.builder()
                .id(1)
                .employee(employee)
                .leaveType(annualLeave)
                .year(2026)
                .totalDays(totalDays)
                .usedDays(usedDays)
                .carryOverDays(carryOverDays)
                .build();
    }

    private LeaveRequest pendingRequest() {
        return LeaveRequest.builder()
                .id(1)
                .employee(employee)
                .leaveType(annualLeave)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 1))
                .days(new BigDecimal("1.0"))
                .reason("Family matter")
                .status("PENDING")
                .build();
    }
}
