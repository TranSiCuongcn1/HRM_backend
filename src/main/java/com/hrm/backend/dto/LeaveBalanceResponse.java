package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {

    private Integer id;
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;
    private Integer leaveTypeId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private int year;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
    private BigDecimal carryOverDays;
    private BigDecimal remainingDays; // Tính toán: totalDays + carryOverDays - usedDays
}
