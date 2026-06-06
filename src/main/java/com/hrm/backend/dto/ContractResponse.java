package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record ContractResponse(
    Integer id,
    Integer employeeId,
    String employeeCode,
    String employeeName,
    String contractType,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal basicSalary,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
