package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record LeaveRequestResponse(
    Integer id,
    Integer employeeId,
    String employeeCode,
    String employeeName,
    Integer leaveTypeId,
    String leaveTypeCode,
    String leaveTypeName,
    Boolean isPaidLeave,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal days,
    String halfDaySession,
    String reason,
    String attachmentUrl,
    String status,
    String approvedByName,
    LocalDateTime approvedAt,
    String rejectionReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
