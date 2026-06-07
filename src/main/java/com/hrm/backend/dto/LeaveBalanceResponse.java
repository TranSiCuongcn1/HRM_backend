package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder(toBuilder = true)
public record LeaveBalanceResponse(
    Integer id,
    Integer employeeId,
    String employeeCode,
    String employeeName,
    Integer leaveTypeId,
    String leaveTypeCode,
    String leaveTypeName,
    int year,
    BigDecimal totalDays,
    BigDecimal usedDays,
    BigDecimal carryOverDays,
    BigDecimal remainingDays // Tính toán: totalDays + carryOverDays - usedDays
) {}
