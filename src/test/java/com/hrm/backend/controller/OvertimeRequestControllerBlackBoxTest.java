package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.OvertimeRequestRequest;
import com.hrm.backend.dto.OvertimeRequestResponse;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.OvertimeRequestService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class OvertimeRequestControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private OvertimeRequestService overtimeRequestService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OvertimeRequestController overtimeRequestController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(overtimeRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/overtime-requests - Valid payload should create overtime request")
    void createRequest_ValidPayload_ReturnsCreated() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(overtimeRequestService.createRequest(eq("employee"), any(OvertimeRequestRequest.class)))
                .thenReturn(standardResponse("PENDING"));

        mockMvc.perform(post("/api/v1/overtime-requests")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.hours").value(2.5));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/overtime-requests - Business rule violation should return 400 Bad Request")
    void createRequest_BusinessRuleViolation_ReturnsBadRequest() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(overtimeRequestService.createRequest(eq("employee"), any(OvertimeRequestRequest.class)))
                .thenThrow(new IllegalArgumentException("End time must be after start time"));

        mockMvc.perform(post("/api/v1/overtime-requests")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("End time must be after start time"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/overtime-requests/my - Returns current employee overtime requests")
    void getMyRequests_ReturnsPagedRequests() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(overtimeRequestService.getMyRequests(
                eq("employee"), eq("PENDING"), eq("project"), any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("PENDING")));

        mockMvc.perform(get("/api/v1/overtime-requests/my")
                        .principal(authentication)
                        .param("status", "PENDING")
                        .param("keyword", "project")
                        .param("sortBy", "date")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.content[0].employeeCode").value("EMP0001"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/overtime-requests - Admin should get all overtime requests")
    void getAllRequests_ReturnsPagedRequests() throws Exception {
        when(overtimeRequestService.getAllRequests(eq("APPROVED"), eq("nguyen"), any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("APPROVED")));

        mockMvc.perform(get("/api/v1/overtime-requests")
                        .param("status", "APPROVED")
                        .param("keyword", "nguyen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("APPROVED"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/overtime-requests/{id}/approve - Should approve overtime request")
    void approveRequest_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("admin");
        when(overtimeRequestService.approveRequest(1, "admin"))
                .thenReturn(standardResponse("APPROVED"));

        mockMvc.perform(put("/api/v1/overtime-requests/{id}/approve", 1)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/overtime-requests/{id}/reject - Should reject overtime request with reason")
    void rejectRequest_ReturnsOk() throws Exception {
        OvertimeRequestResponse rejected = standardResponse("REJECTED");
        rejected.setRejectionReason("Insufficient justification");
        when(authentication.getName()).thenReturn("admin");
        when(overtimeRequestService.rejectRequest(1, "admin", "Insufficient justification"))
                .thenReturn(rejected);

        mockMvc.perform(put("/api/v1/overtime-requests/{id}/reject", 1)
                        .principal(authentication)
                        .param("reason", "Insufficient justification"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Insufficient justification"));
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/overtime-requests/{id} - Should cancel own overtime request")
    void cancelRequest_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("employee");

        mockMvc.perform(delete("/api/v1/overtime-requests/{id}", 1)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(overtimeRequestService).cancelRequest(1, "employee");
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/overtime-requests/{id} - Runtime exception should use fallback 500")
    void cancelRequest_ServiceFailure_ReturnsInternalServerError() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        doThrow(new RuntimeException("Request already processed"))
                .when(overtimeRequestService).cancelRequest(1, "employee");

        mockMvc.perform(delete("/api/v1/overtime-requests/{id}", 1)
                        .principal(authentication))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Lỗi hệ thống: Request already processed"));
    }

    private PageImpl<OvertimeRequestResponse> pageOf(OvertimeRequestResponse response) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
    }

    private OvertimeRequestRequest standardRequest() {
        return OvertimeRequestRequest.builder()
                .date(LocalDate.of(2026, 6, 2))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 30))
                .reason("Project release support")
                .build();
    }

    private OvertimeRequestResponse standardResponse(String status) {
        return OvertimeRequestResponse.builder()
                .id(1)
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .date(LocalDate.of(2026, 6, 2))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 30))
                .hours(new BigDecimal("2.5"))
                .reason("Project release support")
                .status(status)
                .build();
    }
}
