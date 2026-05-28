package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.AttendanceRequest;
import com.hrm.backend.dto.AttendanceResponse;
import com.hrm.backend.dto.CheckInRequest;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.AttendanceService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/attendance/check-in - Should pass GPS and forwarded IP to service")
    void checkIn_WithGpsAndForwardedIp_ReturnsCreated() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(attendanceService.checkIn(
                eq("employee"),
                eq(new BigDecimal("10.848031")),
                eq(new BigDecimal("106.784944")),
                eq("203.0.113.10")))
                .thenReturn(standardResponse("ON_TIME"));

        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .principal(authentication)
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CheckInRequest(new BigDecimal("10.848031"), new BigDecimal("106.784944")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ON_TIME"))
                .andExpect(jsonPath("$.data.checkInIp").value("203.0.113.10"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/attendance/check-out - Should return updated attendance record")
    void checkOut_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(attendanceService.checkOut(eq("employee"), any(), any(), eq("127.0.0.1")))
                .thenReturn(standardResponse("ON_TIME"));

        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CheckInRequest(new BigDecimal("10.848031"), new BigDecimal("106.784944")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workHours").value(8.0));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/attendance/check-in - Business rule violation should return 400 Bad Request")
    void checkIn_BusinessRuleViolation_ReturnsBadRequest() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(attendanceService.checkIn(eq("employee"), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Already checked in today"));

        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckInRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Already checked in today"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/today - Should return current employee today record")
    void getMyToday_ReturnsOk() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(attendanceService.getMyToday("employee")).thenReturn(standardResponse("ON_TIME"));

        mockMvc.perform(get("/api/v1/attendance/today")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP0001"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/my-records - Should return current employee paged records")
    void getMyRecords_ReturnsPagedRecords() throws Exception {
        when(authentication.getName()).thenReturn("employee");
        when(attendanceService.getMyRecords(
                eq("employee"),
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30)),
                eq("ON_TIME"),
                any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("ON_TIME")));

        mockMvc.perform(get("/api/v1/attendance/my-records")
                        .principal(authentication)
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("status", "ON_TIME")
                        .param("sortBy", "date")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("ON_TIME"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/attendance/{id} - Admin should update attendance record")
    void adminUpdateRecord_ReturnsOk() throws Exception {
        AttendanceRequest request = new AttendanceRequest(
                1,
                LocalDate.of(2026, 6, 2),
                LocalTime.of(8, 15),
                LocalTime.of(17, 30),
                "LATE",
                "Manual correction"
        );
        AttendanceResponse response = standardResponse("LATE");
        response.setLateMinutes(15);
        when(attendanceService.adminUpdateRecord(eq(1), any(AttendanceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/attendance/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("LATE"))
                .andExpect(jsonPath("$.data.lateMinutes").value(15));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/attendance/mark-absent - Admin should mark employee absent")
    void markAbsent_ReturnsOk() throws Exception {
        AttendanceResponse response = standardResponse("ABSENT");
        response.setNote("No show");
        when(attendanceService.markAbsent(1, LocalDate.of(2026, 6, 3), "No show"))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/attendance/mark-absent")
                        .param("employeeId", "1")
                        .param("date", "2026-06-03")
                        .param("note", "No show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ABSENT"))
                .andExpect(jsonPath("$.data.note").value("No show"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/employee/{employeeId} - Admin should get employee records")
    void getRecordsByEmployee_ReturnsPagedRecords() throws Exception {
        when(attendanceService.getRecordsByEmployee(
                eq(1),
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30)),
                eq("LATE"),
                any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("LATE")));

        mockMvc.perform(get("/api/v1/attendance/employee/{employeeId}", 1)
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("status", "LATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("LATE"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/daily - Admin should get company daily records")
    void getRecordsByDate_ReturnsPagedRecords() throws Exception {
        when(attendanceService.getRecordsByDate(
                eq(LocalDate.of(2026, 6, 2)),
                eq("ON_TIME"),
                eq("nguyen"),
                eq(true),
                any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("ON_TIME")));

        mockMvc.perform(get("/api/v1/attendance/daily")
                        .param("date", "2026-06-02")
                        .param("status", "ON_TIME")
                        .param("keyword", "nguyen")
                        .param("hasOvertime", "true")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].employeeName").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/range - Admin should get company records in date range")
    void getRecordsByDateRange_ReturnsPagedRecords() throws Exception {
        when(attendanceService.getRecordsByDateRange(
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30)),
                eq("ON_TIME"),
                eq("nguyen"),
                eq(false),
                any(Pageable.class)))
                .thenReturn(pageOf(standardResponse("ON_TIME")));

        mockMvc.perform(get("/api/v1/attendance/range")
                        .param("fromDate", "2026-06-01")
                        .param("toDate", "2026-06-30")
                        .param("status", "ON_TIME")
                        .param("keyword", "nguyen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].date").value("2026-06-02"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/stats/{employeeId} - Admin should get monthly stats")
    void getMonthlyStats_ReturnsOk() throws Exception {
        AttendanceResponse.MonthlyStats stats = AttendanceResponse.MonthlyStats.builder()
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .month(6)
                .year(2026)
                .totalWorkDays(22)
                .lateCount(2)
                .totalOvertimeHours(new BigDecimal("5.5"))
                .build();
        when(attendanceService.getMonthlyStats(1, 6, 2026)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/attendance/stats/{employeeId}", 1)
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalWorkDays").value(22))
                .andExpect(jsonPath("$.data.totalOvertimeHours").value(5.5));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/attendance/my-records - Missing date parameter should return fallback 500")
    void getMyRecords_MissingRequiredDate_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/my-records")
                        .principal(authentication)
                        .param("from", "2026-06-01"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private PageImpl<AttendanceResponse> pageOf(AttendanceResponse response) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
    }

    private AttendanceResponse standardResponse(String status) {
        return AttendanceResponse.builder()
                .id(1)
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .date(LocalDate.of(2026, 6, 2))
                .checkIn(LocalTime.of(8, 0))
                .checkOut(LocalTime.of(17, 0))
                .status(status)
                .workHours(new BigDecimal("8.0"))
                .overtimeHours(new BigDecimal("0.0"))
                .lateMinutes(0)
                .earlyLeaveMinutes(0)
                .checkInIp("203.0.113.10")
                .checkOutIp("127.0.0.1")
                .checkInLat(new BigDecimal("10.848031"))
                .checkInLng(new BigDecimal("106.784944"))
                .checkInGpsValid(true)
                .checkInIpValid(true)
                .checkOutGpsValid(true)
                .checkOutIpValid(true)
                .build();
    }
}
