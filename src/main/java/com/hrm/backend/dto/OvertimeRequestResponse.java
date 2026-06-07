package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder(toBuilder = true)
public record OvertimeRequestResponse(
    Integer id,
    Integer employeeId,
    String employeeCode,
    String employeeName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    BigDecimal hours,
    String reason,
    String status,
    Integer approvedById,
    String approvedByName,
    LocalDateTime approvedAt,
    String rejectionReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
