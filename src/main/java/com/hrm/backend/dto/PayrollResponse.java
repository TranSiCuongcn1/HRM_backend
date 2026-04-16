package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollResponse {

    private Integer id;

    // Thông tin nhân viên
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String month;

    // Lương cơ bản
    private BigDecimal basicSalary;
    private BigDecimal workDays;
    private BigDecimal actualDays;

    // Phụ cấp
    private Map<String, BigDecimal> allowances;
    private BigDecimal totalAllowances;

    // Tăng ca
    private BigDecimal overtimePay;

    // Lương gộp
    private BigDecimal grossSalary;

    // Khấu trừ
    private Map<String, BigDecimal> deductions;
    private BigDecimal totalDeductions;

    // Lương thực nhận
    private BigDecimal netSalary;

    // Workflow
    private String status;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
