package com.hrm.backend.dto;

import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request body cho Admin khi sửa lỗi chấm công
 */
@Builder
public record AttendanceRequest(
    Integer employeeId,
    LocalDate date,
    LocalTime checkIn,
    LocalTime checkOut,
    String status,
    String note
) {}
