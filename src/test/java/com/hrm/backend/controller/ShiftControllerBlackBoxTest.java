package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.ShiftDto;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.ShiftService;
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

import java.time.LocalTime;
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
class ShiftControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private ShiftController shiftController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(shiftController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box GET /api/v1/shifts - Returns shift list")
    void getAll_ReturnsShifts() throws Exception {
        when(shiftService.getAllShifts()).thenReturn(List.of(standardShift()));

        mockMvc.perform(get("/api/v1/shifts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].code").value("OFFICE"))
                .andExpect(jsonPath("$.data[0].isDefault").value(true));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/shifts - Valid payload should create shift")
    void create_ValidPayload_ReturnsOk() throws Exception {
        when(shiftService.createShift(any(ShiftDto.class))).thenReturn(standardShift());

        mockMvc.perform(post("/api/v1/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardShift())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("OFFICE"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/shifts - Duplicate code runtime error currently maps to 500 fallback")
    void create_DuplicateCode_ReturnsInternalServerError() throws Exception {
        when(shiftService.createShift(any(ShiftDto.class)))
                .thenThrow(new RuntimeException("Code already exists"));

        mockMvc.perform(post("/api/v1/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardShift())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/shifts/{id} - Valid payload should update shift")
    void update_ValidPayload_ReturnsOk() throws Exception {
        ShiftDto updated = standardShift();
        updated.setName("Office Shift Updated");
        when(shiftService.updateShift(eq(1), any(ShiftDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/shifts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Office Shift Updated"));
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/shifts/{id} - Should call delete service")
    void delete_ReturnsOk() throws Exception {
        doNothing().when(shiftService).deleteShift(1);

        mockMvc.perform(delete("/api/v1/shifts/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(shiftService).deleteShift(1);
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/shifts/{id} - Runtime error currently maps to 500 fallback")
    void delete_RuntimeError_ReturnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("Shift not found")).when(shiftService).deleteShift(1);

        mockMvc.perform(delete("/api/v1/shifts/{id}", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private ShiftDto standardShift() {
        ShiftDto dto = new ShiftDto();
        dto.setId(1);
        dto.setCode("OFFICE");
        dto.setName("Office Shift");
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(17, 0));
        dto.setBreakStartTime(LocalTime.of(12, 0));
        dto.setBreakEndTime(LocalTime.of(13, 0));
        dto.setIsDefault(true);
        dto.setIsActive(true);
        return dto;
    }
}
