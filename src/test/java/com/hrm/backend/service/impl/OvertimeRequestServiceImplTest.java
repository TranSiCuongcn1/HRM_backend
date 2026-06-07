package com.hrm.backend.service.impl;

import com.hrm.backend.dto.OvertimeRequestRequest;
import com.hrm.backend.dto.OvertimeRequestResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.OvertimeRequest;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.OvertimeRequestRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.EmailService;
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
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeRequestServiceImplTest {

    @Mock
    private OvertimeRequestRepository overtimeRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OvertimeRequestServiceImpl overtimeRequestService;

    private Employee employee;
    private Employee admin;
    private User employeeUser;
    private User adminUser;

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
        employeeUser = User.builder().username("employee").employee(employee).build();
        adminUser = User.builder().username("admin").employee(admin).build();
    }

    @Test
    @DisplayName("Unit createRequest - Valid time range should create PENDING request with calculated hours")
    void createRequest_ValidTimeRange_CreatesPendingRequest() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(overtimeRequestRepository.countOverlappingRequests(1, LocalDate.of(2026, 6, 2), LocalTime.of(18, 0), LocalTime.of(20, 30)))
                .thenReturn(0L);
        when(overtimeRequestRepository.save(any(OvertimeRequest.class))).thenAnswer(invocation -> {
            OvertimeRequest request = invocation.getArgument(0);
            request.setId(1);
            return request;
        });

        OvertimeRequestResponse response = overtimeRequestService.createRequest("employee", standardRequest());

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.hours()).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(response.employeeCode()).isEqualTo("EMP0001");

        ArgumentCaptor<OvertimeRequest> captor = ArgumentCaptor.forClass(OvertimeRequest.class);
        verify(overtimeRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
        assertThat(captor.getValue().getHours()).isEqualByComparingTo(new BigDecimal("2.50"));
    }

    @Test
    @DisplayName("Unit createRequest - End time before start time should throw IllegalArgumentException")
    void createRequest_EndBeforeStart_ThrowsException() {
        OvertimeRequestRequest request = standardRequestBuilder()
                .startTime(LocalTime.of(20, 30))
                .endTime(LocalTime.of(18, 0))
                .build();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));

        assertThatThrownBy(() -> overtimeRequestService.createRequest("employee", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kết thúc");

        verify(overtimeRequestRepository, never()).save(any(OvertimeRequest.class));
    }

    @Test
    @DisplayName("Unit createRequest - Overlapping time range should throw IllegalArgumentException")
    void createRequest_OverlappingTimeRange_ThrowsException() {
        OvertimeRequestRequest request = standardRequest();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(overtimeRequestRepository.countOverlappingRequests(1, request.date(), request.startTime(), request.endTime()))
                .thenReturn(1L);

        assertThatThrownBy(() -> overtimeRequestService.createRequest("employee", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("trùng khoảng thời gian này");

        verify(overtimeRequestRepository, never()).save(any(OvertimeRequest.class));
    }

    @Test
    @DisplayName("Unit approveRequest - Pending request should become APPROVED")
    void approveRequest_PendingRequest_ApprovesRequest() {
        OvertimeRequest pending = pendingRequest();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(overtimeRequestRepository.findById(1)).thenReturn(Optional.of(pending));
        when(overtimeRequestRepository.save(any(OvertimeRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OvertimeRequestResponse response = overtimeRequestService.approveRequest(1, "admin");

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.approvedByName()).isEqualTo("Admin User");
        assertThat(response.approvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Unit approveRequest - Non-pending request should throw RuntimeException")
    void approveRequest_NonPendingRequest_ThrowsException() {
        OvertimeRequest approved = pendingRequest();
        approved.setStatus("APPROVED");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(overtimeRequestRepository.findById(1)).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> overtimeRequestService.approveRequest(1, "admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PENDING");

        verify(overtimeRequestRepository, never()).save(any(OvertimeRequest.class));
    }

    @Test
    @DisplayName("Unit rejectRequest - Pending request should become REJECTED with reason")
    void rejectRequest_PendingRequest_RejectsRequest() {
        OvertimeRequest pending = pendingRequest();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(overtimeRequestRepository.findById(1)).thenReturn(Optional.of(pending));
        when(overtimeRequestRepository.save(any(OvertimeRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OvertimeRequestResponse response = overtimeRequestService.rejectRequest(1, "admin", "Too late");

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.rejectionReason()).isEqualTo("Too late");
        assertThat(response.approvedByName()).isEqualTo("Admin User");
    }

    @Test
    @DisplayName("Unit cancelRequest - Owner can cancel pending request")
    void cancelRequest_OwnerPendingRequest_CancelsRequest() {
        OvertimeRequest pending = pendingRequest();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(overtimeRequestRepository.findById(1)).thenReturn(Optional.of(pending));
        when(overtimeRequestRepository.save(any(OvertimeRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        overtimeRequestService.cancelRequest(1, "employee");

        assertThat(pending.getStatus()).isEqualTo("CANCELLED");
        verify(overtimeRequestRepository).save(pending);
    }

    @Test
    @DisplayName("Unit cancelRequest - Non-owner cannot cancel request")
    void cancelRequest_NonOwner_ThrowsException() {
        OvertimeRequest pending = pendingRequest();
        pending.setEmployee(Employee.builder().id(99).code("EMP0099").name("Other").build());
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(overtimeRequestRepository.findById(1)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> overtimeRequestService.cancelRequest(1, "employee"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("không có quyền");

        verify(overtimeRequestRepository, never()).save(any(OvertimeRequest.class));
    }

    @Test
    @DisplayName("Unit cancelRequest - Non-pending request cannot be cancelled")
    void cancelRequest_NonPendingRequest_ThrowsException() {
        OvertimeRequest approved = pendingRequest();
        approved.setStatus("APPROVED");
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employeeUser));
        when(overtimeRequestRepository.findById(1)).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> overtimeRequestService.cancelRequest(1, "employee"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PENDING");

        verify(overtimeRequestRepository, never()).save(any(OvertimeRequest.class));
    }

    private OvertimeRequestRequest.OvertimeRequestRequestBuilder standardRequestBuilder() {
        return OvertimeRequestRequest.builder()
                .date(LocalDate.of(2026, 6, 2))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 30))
                .reason("Project release");
    }

    private OvertimeRequestRequest standardRequest() {
        return standardRequestBuilder().build();
    }

    private OvertimeRequest pendingRequest() {
        return OvertimeRequest.builder()
                .id(1)
                .employee(employee)
                .date(LocalDate.of(2026, 6, 2))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 30))
                .hours(new BigDecimal("2.50"))
                .reason("Project release")
                .status("PENDING")
                .build();
    }
}
