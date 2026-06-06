package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.dto.ContractResponse;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContractControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(contractController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/contracts - Valid payload should create DRAFT contract")
    void createContract_ValidPayload_ReturnsCreated() throws Exception {
        when(contractService.createContract(any(ContractRequest.class))).thenReturn(standardResponse("DRAFT"));

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/contracts - Missing required fields should return validation errors")
    void createContract_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        ContractRequest request = ContractRequest.builder()
                .employeeId(null)
                .contractType("")
                .startDate(null)
                .basicSalary(null)
                .build();

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.employeeId").exists())
                .andExpect(jsonPath("$.data.contractType").exists())
                .andExpect(jsonPath("$.data.startDate").exists())
                .andExpect(jsonPath("$.data.basicSalary").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/contracts - Non-positive salary should return validation error")
    void createContract_NonPositiveSalary_ReturnsBadRequest() throws Exception {
        ContractRequest request = standardRequestBuilder()
                .basicSalary(BigDecimal.ZERO)
                .build();

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.basicSalary").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/contracts - Business rule violation should return 400 Bad Request")
    void createContract_BusinessRuleViolation_ReturnsBadRequest() throws Exception {
        when(contractService.createContract(any(ContractRequest.class)))
                .thenThrow(new IllegalArgumentException("Cannot create contract for resigned employee"));

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot create contract for resigned employee"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/contracts/{id} - Valid payload should update contract")
    void updateContract_ValidPayload_ReturnsOk() throws Exception {
        ContractResponse updated = standardResponse("DRAFT").toBuilder()
                .basicSalary(new BigDecimal("22000000"))
                .build();
        when(contractService.updateContract(eq(1), any(ContractRequest.class))).thenReturn(updated);

        ContractRequest request = standardRequestBuilder()
                .basicSalary(new BigDecimal("22000000"))
                .build();

        mockMvc.perform(put("/api/v1/contracts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basicSalary").value(22000000));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/contracts/{id}/activate - Should activate contract")
    void activateContract_ReturnsOk() throws Exception {
        when(contractService.activateContract(1)).thenReturn(standardResponse("ACTIVE"));

        mockMvc.perform(put("/api/v1/contracts/{id}/activate", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/contracts/{id}/terminate - Should terminate contract")
    void terminateContract_ReturnsOk() throws Exception {
        when(contractService.terminateContract(1)).thenReturn(standardResponse("TERMINATED"));

        mockMvc.perform(put("/api/v1/contracts/{id}/terminate", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("TERMINATED"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/contracts/{id} - Returns contract detail")
    void getContractById_ReturnsContract() throws Exception {
        when(contractService.getContractById(1)).thenReturn(standardResponse("ACTIVE"));

        mockMvc.perform(get("/api/v1/contracts/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP0001"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/contracts/employee/{employeeId} - Returns employee contract history")
    void getContractsByEmployee_ReturnsContracts() throws Exception {
        when(contractService.getContractsByEmployee(1)).thenReturn(List.of(standardResponse("ACTIVE")));

        mockMvc.perform(get("/api/v1/contracts/employee/{employeeId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeId").value(1));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/contracts/employee/{employeeId}/active - Returns active contract")
    void getActiveContract_ReturnsContract() throws Exception {
        when(contractService.getActiveContract(1)).thenReturn(standardResponse("ACTIVE"));

        mockMvc.perform(get("/api/v1/contracts/employee/{employeeId}/active", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/contracts/expiring - Returns contracts expiring in requested days")
    void getExpiringContracts_ReturnsContracts() throws Exception {
        when(contractService.getExpiringContracts(15)).thenReturn(List.of(standardResponse("ACTIVE")));

        mockMvc.perform(get("/api/v1/contracts/expiring").param("days", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    private ContractRequest.ContractRequestBuilder standardRequestBuilder() {
        return ContractRequest.builder()
                .employeeId(1)
                .contractType("DEFINITE_1YR")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .basicSalary(new BigDecimal("20000000"));
    }

    private ContractRequest standardRequest() {
        return standardRequestBuilder().build();
    }

    private ContractResponse standardResponse(String status) {
        return ContractResponse.builder()
                .id(1)
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .contractType("DEFINITE_1YR")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .basicSalary(new BigDecimal("20000000"))
                .status(status)
                .build();
    }
}
