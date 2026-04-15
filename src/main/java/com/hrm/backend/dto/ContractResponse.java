package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {

    private Integer id;
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;
    private String contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal basicSalary;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
