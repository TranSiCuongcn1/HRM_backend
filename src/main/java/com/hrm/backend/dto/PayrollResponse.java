package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Builder(toBuilder = true)
public record PayrollResponse(
    Integer id,
    Integer employeeId,
    String employeeCode,
    String employeeName,
    String departmentName,
    String month,
    BigDecimal basicSalary,
    BigDecimal workDays,
    BigDecimal actualDays,
    Map<String, BigDecimal> allowances,
    BigDecimal totalAllowances,
    BigDecimal overtimePay,
    BigDecimal grossSalary,
    Map<String, BigDecimal> deductions,
    BigDecimal totalDeductions,
    BigDecimal netSalary,
    String status,
    String approvedByName,
    LocalDateTime approvedAt,
    LocalDateTime paidAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
