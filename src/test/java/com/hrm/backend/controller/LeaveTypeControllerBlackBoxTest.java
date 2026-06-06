package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.backend.dto.LeaveTypeRequest;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.LeaveTypeService;
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
class LeaveTypeControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LeaveTypeService leaveTypeService;

    @InjectMocks
    private LeaveTypeController leaveTypeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(leaveTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box GET /api/v1/leave-types - Returns leave type list")
    void getAllLeaveTypes_ReturnsTypes() throws Exception {
        when(leaveTypeService.getAllLeaveTypes()).thenReturn(List.of(standardLeaveType()));

        mockMvc.perform(get("/api/v1/leave-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].code").value("ANNUAL"))
                .andExpect(jsonPath("$.data[0].isPaid").value(true));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-types - Valid payload should create leave type")
    void createLeaveType_ValidPayload_ReturnsCreated() throws Exception {
        when(leaveTypeService.createLeaveType(any(LeaveTypeRequest.class))).thenReturn(standardLeaveType());

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("ANNUAL"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-types - Missing code and name should return validation errors")
    void createLeaveType_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        LeaveTypeRequest request = standardRequestBuilder()
                .code("")
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").exists())
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/leave-types - Duplicate code should return 400 Bad Request")
    void createLeaveType_DuplicateCode_ReturnsBadRequest() throws Exception {
        when(leaveTypeService.createLeaveType(any(LeaveTypeRequest.class)))
                .thenThrow(new IllegalArgumentException("Leave type code already exists"));

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Leave type code already exists"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/leave-types/{id} - Valid payload should update leave type")
    void updateLeaveType_ValidPayload_ReturnsOk() throws Exception {
        LeaveType updated = standardLeaveType();
        updated.setName("Annual Paid Leave");
        when(leaveTypeService.updateLeaveType(eq(1), any(LeaveTypeRequest.class))).thenReturn(updated);

        LeaveTypeRequest request = standardRequestBuilder()
                .name("Annual Paid Leave")
                .build();

        mockMvc.perform(put("/api/v1/leave-types/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Annual Paid Leave"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/leave-types/{id} - Missing name should return validation error")
    void updateLeaveType_MissingName_ReturnsBadRequest() throws Exception {
        LeaveTypeRequest request = standardRequestBuilder()
                .name("")
                .build();

        mockMvc.perform(put("/api/v1/leave-types/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists());
    }

    private LeaveTypeRequest.LeaveTypeRequestBuilder standardRequestBuilder() {
        return LeaveTypeRequest.builder()
                .code("ANNUAL")
                .name("Annual Leave")
                .isPaid(true)
                .description("Paid annual leave");
    }

    private LeaveTypeRequest standardRequest() {
        return standardRequestBuilder().build();
    }

    private LeaveType standardLeaveType() {
        return LeaveType.builder()
                .id(1)
                .code("ANNUAL")
                .name("Annual Leave")
                .isPaid(true)
                .description("Paid annual leave")
                .build();
    }
}
