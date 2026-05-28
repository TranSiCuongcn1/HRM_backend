package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.HolidayDto;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.HolidayService;
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
class HolidayControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private HolidayController holidayController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(holidayController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box GET /api/v1/holidays - Returns holiday list")
    void getAll_ReturnsHolidays() throws Exception {
        when(holidayService.getAllHolidays()).thenReturn(List.of(standardHoliday()));

        mockMvc.perform(get("/api/v1/holidays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Tet Holiday"))
                .andExpect(jsonPath("$.data[0].isPaid").value(true));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/holidays - Valid payload should create holiday")
    void create_ValidPayload_ReturnsOk() throws Exception {
        when(holidayService.createHoliday(any(HolidayDto.class))).thenReturn(standardHoliday());

        mockMvc.perform(post("/api/v1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardHoliday())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Tet Holiday"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/holidays - Duplicate date runtime error currently maps to 500 fallback")
    void create_DuplicateDate_ReturnsInternalServerError() throws Exception {
        when(holidayService.createHoliday(any(HolidayDto.class)))
                .thenThrow(new RuntimeException("Date already exists"));

        mockMvc.perform(post("/api/v1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardHoliday())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/holidays/batch - Valid payload should create holiday batch")
    void createBatch_ValidPayload_ReturnsOk() throws Exception {
        when(holidayService.createHolidays(any())).thenReturn(List.of(standardHoliday()));

        mockMvc.perform(post("/api/v1/holidays/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(standardHoliday()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].date").value("2026-02-17"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/holidays/{id} - Valid payload should update holiday")
    void update_ValidPayload_ReturnsOk() throws Exception {
        HolidayDto updated = standardHoliday();
        updated.setName("Tet Holiday Updated");
        when(holidayService.updateHoliday(eq(1), any(HolidayDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/holidays/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Tet Holiday Updated"));
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/holidays/{id} - Should call delete service")
    void delete_ReturnsOk() throws Exception {
        doNothing().when(holidayService).deleteHoliday(1);

        mockMvc.perform(delete("/api/v1/holidays/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(holidayService).deleteHoliday(1);
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/holidays/{id} - Runtime error currently maps to 500 fallback")
    void delete_RuntimeError_ReturnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("Holiday not found")).when(holidayService).deleteHoliday(1);

        mockMvc.perform(delete("/api/v1/holidays/{id}", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private HolidayDto standardHoliday() {
        HolidayDto dto = new HolidayDto();
        dto.setId(1);
        dto.setName("Tet Holiday");
        dto.setDate(LocalDate.of(2026, 2, 17));
        dto.setIsPaid(true);
        return dto;
    }
}
