package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.PayrollResponse;
import com.hrm.backend.dto.PayrollUpdateRequest;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PayrollControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PayrollService payrollService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PayrollController payrollController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(payrollController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/payrolls/generate - Admin should generate monthly draft payrolls")
    void generatePayroll_WithDefaultAdjustments_ReturnsCreated() throws Exception {
        Map<String, Object> body = Map.of(
                "defaultAllowances", Map.of("Meal", new BigDecimal("500000")),
                "defaultDeductions", Map.of("Advance", new BigDecimal("100000"))
        );
        when(payrollService.generatePayroll(
                eq(6),
                eq(2026),
                eq(new BigDecimal("22")),
                eq(Map.of("Meal", new BigDecimal("500000"))),
                eq(Map.of("Advance", new BigDecimal("100000")))))
                .thenReturn(List.of(standardResponse("DRAFT")));

        mockMvc.perform(post("/api/v1/payrolls/generate")
                        .param("month", "6")
                        .param("year", "2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.data[0].netSalary").value(19500000));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/payrolls/generate - Business rule violation should return 400 Bad Request")
    void generatePayroll_BusinessRuleViolation_ReturnsBadRequest() throws Exception {
        when(payrollService.generatePayroll(eq(13), eq(2026), eq(new BigDecimal("22")), eq(null), eq(null)))
                .thenThrow(new IllegalArgumentException("Month must be between 1 and 12"));

        mockMvc.perform(post("/api/v1/payrolls/generate")
                        .param("month", "13")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/payrolls/{id} - Admin should update draft payroll")
    void updatePayroll_ReturnsOk() throws Exception {
        PayrollUpdateRequest request = new PayrollUpdateRequest(
                new BigDecimal("22"),
                new BigDecimal("21"),
                Map.of("Meal", new BigDecimal("500000")),
                Map.of("Advance", new BigDecimal("100000")),
                new BigDecimal("750000")
        );
        PayrollResponse response = standardResponse("DRAFT");
        response.setOvertimePay(new BigDecimal("750000"));
        when(payrollService.updatePayroll(eq(1), any(PayrollUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/payrolls/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overtimePay").value(750000));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/payrolls/bulk-update - Admin should update multiple draft payrolls")
    void bulkUpdatePayroll_ReturnsOk() throws Exception {
        PayrollUpdateRequest.BulkUpdateRequest request = new PayrollUpdateRequest.BulkUpdateRequest(
                List.of(1, 2),
                Map.of("Phone", new BigDecimal("300000")),
                Map.of("Union", new BigDecimal("50000"))
        );
        when(payrollService.bulkUpdatePayroll(any(PayrollUpdateRequest.BulkUpdateRequest.class)))
                .thenReturn(List.of(standardResponse("DRAFT"), standardResponse("DRAFT")));

        mockMvc.perform(put("/api/v1/payrolls/bulk-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/payrolls/{id}/submit - Admin should submit payroll")
    void submitPayroll_ReturnsOk() throws Exception {
        when(payrollService.submitPayroll(1)).thenReturn(standardResponse("CALCULATED"));

        mockMvc.perform(put("/api/v1/payrolls/{id}/submit", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/payrolls/{id}/approve - Admin should approve payroll with username")
    void approvePayroll_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("admin");
        when(payrollService.approvePayroll("admin", 1)).thenReturn(standardResponse("APPROVED"));

        mockMvc.perform(put("/api/v1/payrolls/{id}/approve", 1)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/payrolls/{id}/pay - Admin should mark payroll as paid")
    void markAsPaid_ReturnsOk() throws Exception {
        when(payrollService.markAsPaid(1)).thenReturn(standardResponse("PAID"));

        mockMvc.perform(put("/api/v1/payrolls/{id}/pay", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/payrolls - Admin should get monthly payroll page with filters")
    void getPayrollsByMonth_ReturnsPagedPayrolls() throws Exception {
        when(payrollService.getPayrollsByMonth(
                eq("2026-06"),
                eq("DRAFT"),
                eq("nguyen"),
                eq(1),
                any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("DRAFT")));

        mockMvc.perform(get("/api/v1/payrolls")
                        .param("month", "2026-06")
                        .param("status", "DRAFT")
                        .param("keyword", "nguyen")
                        .param("departmentId", "1")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].month").value("2026-06"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/payrolls/{id} - Should get payroll detail")
    void getPayrollById_ReturnsOk() throws Exception {
        when(payrollService.getPayrollById(1)).thenReturn(standardResponse("APPROVED"));

        mockMvc.perform(get("/api/v1/payrolls/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP0001"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/payrolls/employee/{employeeId} - Admin should get any employee payrolls")
    void getPayrollsByEmployee_AsAdmin_ReturnsPagedPayrolls() throws Exception {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        when(payrollService.getPayrollsByEmployee(eq(1), eq("PAID"), any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("PAID")));

        mockMvc.perform(get("/api/v1/payrolls/employee/{employeeId}", 1)
                        .principal(authentication)
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PAID"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/payrolls/employee/{employeeId} - Employee should get own payrolls")
    void getPayrollsByEmployee_AsOwner_ReturnsPagedPayrolls() throws Exception {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("employee");
        when(payrollService.getUserByUsername("employee")).thenReturn(userWithEmployeeId(1));
        when(payrollService.getPayrollsByEmployee(eq(1), eq(null), any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("PAID")));

        mockMvc.perform(get("/api/v1/payrolls/employee/{employeeId}", 1)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PAID"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/payrolls/employee/{employeeId} - Employee cannot view other employee payrolls")
    void getPayrollsByEmployee_AsDifferentEmployee_ReturnsForbidden() throws Exception {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("employee");
        when(payrollService.getUserByUsername("employee")).thenReturn(userWithEmployeeId(1));

        mockMvc.perform(get("/api/v1/payrolls/employee/{employeeId}", 2)
                        .principal(authentication))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(payrollService, never()).getPayrollsByEmployee(eq(2), any(), any(Pageable.class));
    }

    private PageImpl<PayrollResponse> pageOf(PayrollResponse response) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
    }

    private User userWithEmployeeId(Integer employeeId) {
        return User.builder()
                .username("employee")
                .employee(Employee.builder().id(employeeId).build())
                .build();
    }

    private PayrollResponse standardResponse(String status) {
        return PayrollResponse.builder()
                .id(1)
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .departmentName("Engineering")
                .month("2026-06")
                .basicSalary(new BigDecimal("20000000"))
                .workDays(new BigDecimal("22"))
                .actualDays(new BigDecimal("22"))
                .allowances(Map.of("Meal", new BigDecimal("500000")))
                .totalAllowances(new BigDecimal("500000"))
                .overtimePay(new BigDecimal("0"))
                .grossSalary(new BigDecimal("20500000"))
                .deductions(Map.of("Advance", new BigDecimal("1000000")))
                .totalDeductions(new BigDecimal("1000000"))
                .netSalary(new BigDecimal("19500000"))
                .status(status)
                .build();
    }
}
