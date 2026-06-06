package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.LeaveRequestDTO;
import com.hrm.backend.dto.LeaveRequestResponse;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.LeaveRequestService;
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
class LeaveRequestControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LeaveRequestService leaveRequestService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LeaveRequestController leaveRequestController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(leaveRequestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-requests - Valid payload should submit leave request")
    void submitRequest_ValidPayload_ReturnsCreated() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(leaveRequestService.submitRequest(eq("employee"), any(LeaveRequestDTO.class)))
                .thenReturn(standardResponse("PENDING"));

        mockMvc.perform(post("/api/v1/leave-requests")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.days").value(1.0));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-requests - Missing required fields should return validation errors")
    void submitRequest_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        LeaveRequestDTO request = standardRequest().toBuilder()
                .leaveTypeId(null)
                .startDate(null)
                .endDate(null)
                .days(null)
                .reason("")
                .build();

        mockMvc.perform(post("/api/v1/leave-requests")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.leaveTypeId").exists())
                .andExpect(jsonPath("$.data.startDate").exists())
                .andExpect(jsonPath("$.data.endDate").exists())
                .andExpect(jsonPath("$.data.days").exists())
                .andExpect(jsonPath("$.data.reason").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-requests - Non-positive days should return validation error")
    void submitRequest_NonPositiveDays_ReturnsBadRequest() throws Exception {
        LeaveRequestDTO request = standardRequest().toBuilder()
                .days(BigDecimal.ZERO)
                .build();

        mockMvc.perform(post("/api/v1/leave-requests")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.days").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-requests - Business rule violation should return 400 Bad Request")
    void submitRequest_BusinessRuleViolation_ReturnsBadRequest() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(leaveRequestService.submitRequest(eq("employee"), any(LeaveRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Insufficient leave balance"));

        mockMvc.perform(post("/api/v1/leave-requests")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient leave balance"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-requests/my - Returns current employee leave requests")
    void getMyRequests_ReturnsPagedRequests() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(leaveRequestService.getMyRequests(
                eq("employee"), eq("PENDING"), eq(1), eq("annual"), any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("PENDING")));

        mockMvc.perform(get("/api/v1/leave-requests/my")
                        .principal(authentication)
                        .param("status", "PENDING")
                        .param("leaveTypeId", "1")
                        .param("keyword", "annual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/leave-requests/{id}/cancel - Should cancel own request")
    void cancelRequest_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(leaveRequestService.cancelRequest("employee", 1)).thenReturn(standardResponse("CANCELLED"));

        mockMvc.perform(put("/api/v1/leave-requests/{id}/cancel", 1)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-requests/pending - Returns pending requests")
    void getPendingRequests_ReturnsPagedRequests() throws Exception {
        when(leaveRequestService.getPendingRequests(any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("PENDING")));

        mockMvc.perform(get("/api/v1/leave-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-requests - Returns all requests with filters")
    void getAllRequests_ReturnsPagedRequests() throws Exception {
        when(leaveRequestService.getAllRequests(eq("APPROVED"), eq(1), eq("nguyen"), any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("APPROVED")));

        mockMvc.perform(get("/api/v1/leave-requests")
                        .param("status", "APPROVED")
                        .param("leaveTypeId", "1")
                        .param("keyword", "nguyen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("APPROVED"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/leave-requests/{id}/approve - Should approve request")
    void approveRequest_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("admin");
        when(leaveRequestService.approveRequest("admin", 1)).thenReturn(standardResponse("APPROVED"));

        mockMvc.perform(put("/api/v1/leave-requests/{id}/approve", 1)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/leave-requests/{id}/reject - Should reject request with reason")
    void rejectRequest_ReturnsOk() throws Exception {
        LeaveRequestResponse rejected = standardResponse("REJECTED").toBuilder()
                .rejectionReason("Invalid evidence")
                .build();
        when(authentication.getName()).thenReturn("admin");
        when(leaveRequestService.rejectRequest("admin", 1, "Invalid evidence")).thenReturn(rejected);

        mockMvc.perform(put("/api/v1/leave-requests/{id}/reject", 1)
                        .principal(authentication)
                        .param("reason", "Invalid evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Invalid evidence"));
    }

    private PageImpl<LeaveRequestResponse> pageOf(LeaveRequestResponse response) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
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

    private LeaveRequestResponse standardResponse(String status) {
        return LeaveRequestResponse.builder()
                .id(1)
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .leaveTypeId(1)
                .leaveTypeCode("ANNUAL")
                .leaveTypeName("Annual Leave")
                .isPaidLeave(true)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 1))
                .days(new BigDecimal("1.0"))
                .reason("Family matter")
                .status(status)
                .build();
    }
}
