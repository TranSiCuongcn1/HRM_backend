package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.dto.EmployeeResponse;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.EmployeeService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box GET /api/v1/employees - Returns paged employees in ApiResponse")
    void getAllEmployees_ReturnsPagedEmployees() throws Exception {
        EmployeeResponse employee = standardResponse();
        when(employeeService.getAllEmployees(eq("EMP"), eq("ACTIVE"), eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/employees")
                        .param("keyword", "EMP")
                        .param("status", "ACTIVE")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("EMP0001"))
                .andExpect(jsonPath("$.data.content[0].email").value("employee@hrm.com"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/employees/{id} - Returns employee detail")
    void getEmployeeById_ReturnsEmployeeDetail() throws Exception {
        when(employeeService.getEmployeeById(1)).thenReturn(standardResponse());

        mockMvc.perform(get("/api/v1/employees/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/employees - Valid payload should create employee")
    void createEmployee_ValidPayload_ReturnsCreated() throws Exception {
        EmployeeResponse created = standardResponse().toBuilder()
                .generatedAccount(EmployeeResponse.AccountInfo.builder()
                        .username("EMP0001")
                        .defaultPassword("Hrm@123456")
                        .role("EMPLOYEE")
                        .build())
                .build();
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.generatedAccount.username").value("EMP0001"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/employees - Invalid email should return validation errors")
    void createEmployee_InvalidEmail_ReturnsBadRequest() throws Exception {
        EmployeeRequest request = standardRequest().toBuilder()
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/employees - Missing required fields should return validation errors")
    void createEmployee_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        EmployeeRequest request = standardRequest().toBuilder()
                .name("")
                .email("")
                .joinDate(null)
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.joinDate").exists());
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/employees/{id} - Valid payload should update employee")
    void updateEmployee_ValidPayload_ReturnsOk() throws Exception {
        EmployeeResponse updated = standardResponse().toBuilder()
                .name("Nguyen Van B")
                .build();
        when(employeeService.updateEmployee(eq(1), any(EmployeeRequest.class))).thenReturn(updated);

        EmployeeRequest request = standardRequest().toBuilder()
                .name("Nguyen Van B")
                .build();

        mockMvc.perform(put("/api/v1/employees/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van B"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/employees/{id}/resign - Should call resign service")
    void resignEmployee_ReturnsOk() throws Exception {
        LocalDate resignationDate = LocalDate.of(2026, 5, 28);
        doNothing().when(employeeService).resignEmployee(1, resignationDate);

        mockMvc.perform(put("/api/v1/employees/{id}/resign", 1)
                        .param("resignationDate", "2026-05-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(employeeService).resignEmployee(1, resignationDate);
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/employees/{id} - Should call delete service")
    void deleteEmployee_ReturnsOk() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/api/v1/employees/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(employeeService).deleteEmployee(1);
    }

    @Test
    @DisplayName("Black-box POST /api/v1/employees - Duplicate email should return 400 Bad Request")
    void createEmployee_DuplicateEmail_ReturnsBadRequest() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/employees/{id} - Business conflict currently maps to 500 fallback")
    void deleteEmployee_BusinessConflict_ReturnsInternalServerError() throws Exception {
        doThrow(new IllegalStateException("Employee has business data"))
                .when(employeeService).deleteEmployee(1);

        mockMvc.perform(delete("/api/v1/employees/{id}", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private EmployeeRequest standardRequest() {
        return EmployeeRequest.builder()
                .code("EMP0001")
                .name("Nguyen Van A")
                .email("employee@hrm.com")
                .phone("0900000000")
                .joinDate(LocalDate.of(2026, 1, 1))
                .departmentId(1)
                .dependentCount(0)
                .build();
    }

    private EmployeeResponse standardResponse() {
        return EmployeeResponse.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .email("employee@hrm.com")
                .phone("0900000000")
                .joinDate(LocalDate.of(2026, 1, 1))
                .departmentId(1)
                .departmentName("IT")
                .status("ACTIVE")
                .dependentCount(0)
                .build();
    }
}
