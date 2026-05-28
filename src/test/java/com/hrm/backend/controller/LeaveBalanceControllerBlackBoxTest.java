package com.hrm.backend.controller;

import com.hrm.backend.dto.LeaveBalanceResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.LeaveBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceControllerBlackBoxTest {

    private MockMvc mockMvc;

    @Mock
    private LeaveBalanceService leaveBalanceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LeaveBalanceController leaveBalanceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaveBalanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-balances/init - Should initialize balances")
    void initBalance_ReturnsOk() throws Exception {
        doNothing().when(leaveBalanceService).initBalanceForEmployee(1, 2026);

        mockMvc.perform(post("/api/v1/leave-balances/init")
                        .param("employeeId", "1")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(leaveBalanceService).initBalanceForEmployee(1, 2026);
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-balances/my - Returns current employee balances")
    void getMyBalances_ReturnsBalances() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(standardUser()));
        when(leaveBalanceService.getBalancesByEmployee(1, 2026)).thenReturn(List.of(standardResponse()));

        mockMvc.perform(get("/api/v1/leave-balances/my")
                        .principal(authentication)
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeId").value(1))
                .andExpect(jsonPath("$.data[0].remainingDays").value(10.0));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-balances/my - Missing user currently maps to 500 fallback")
    void getMyBalances_UserNotFound_ReturnsInternalServerError() throws Exception {
        when(authentication.getName()).thenReturn("missing");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/leave-balances/my")
                        .principal(authentication)
                        .param("year", "2026"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-balances/employee/{employeeId} - Returns employee balances")
    void getEmployeeBalances_ReturnsBalances() throws Exception {
        when(leaveBalanceService.getBalancesByEmployee(1, 2026)).thenReturn(List.of(standardResponse()));

        mockMvc.perform(get("/api/v1/leave-balances/employee/{employeeId}", 1)
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].leaveTypeCode").value("ANNUAL"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/leave-balances/{id} - Should update total and carry-over days")
    void updateBalance_ReturnsUpdatedBalance() throws Exception {
        LeaveBalanceResponse updated = standardResponse();
        updated.setTotalDays(new BigDecimal("15.0"));
        updated.setCarryOverDays(new BigDecimal("2.0"));
        updated.setRemainingDays(new BigDecimal("15.0"));

        when(leaveBalanceService.updateBalance(eq(1), eq(new BigDecimal("15.0")), eq(new BigDecimal("2.0"))))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/leave-balances/{id}", 1)
                        .param("totalDays", "15.0")
                        .param("carryOverDays", "2.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDays").value(15.0))
                .andExpect(jsonPath("$.data.carryOverDays").value(2.0));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-balances/employee/{employeeId} - Service runtime error currently maps to 500 fallback")
    void getEmployeeBalances_ServiceRuntimeError_ReturnsInternalServerError() throws Exception {
        when(leaveBalanceService.getBalancesByEmployee(eq(99), anyInt()))
                .thenThrow(new RuntimeException("Employee not found"));

        mockMvc.perform(get("/api/v1/leave-balances/employee/{employeeId}", 99)
                        .param("year", "2026"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private User standardUser() {
        Employee employee = Employee.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .build();
        return User.builder()
                .id(1)
                .employee(employee)
                .username("employee")
                .email("employee@hrm.com")
                .passwordHash("hash")
                .role("EMPLOYEE")
                .isActive(true)
                .build();
    }

    private LeaveBalanceResponse standardResponse() {
        return LeaveBalanceResponse.builder()
                .id(1)
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .leaveTypeId(1)
                .leaveTypeCode("ANNUAL")
                .leaveTypeName("Annual Leave")
                .year(2026)
                .totalDays(new BigDecimal("12.0"))
                .usedDays(new BigDecimal("2.0"))
                .carryOverDays(BigDecimal.ZERO)
                .remainingDays(new BigDecimal("10.0"))
                .build();
    }
}
