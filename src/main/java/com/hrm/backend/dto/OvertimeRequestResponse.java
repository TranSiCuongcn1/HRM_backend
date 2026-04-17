package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRequestResponse {
    private Integer id;

    // Thông tin nhân viên
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;

    // Chi tiết đơn
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal hours;
    private String reason;

    // Trạng thái & duyệt
    private String status;
    private Integer approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
