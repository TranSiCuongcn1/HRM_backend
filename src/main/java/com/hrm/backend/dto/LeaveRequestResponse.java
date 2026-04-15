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
public class LeaveRequestResponse {

    private Integer id;

    // Thông tin nhân viên
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;

    // Thông tin loại phép
    private Integer leaveTypeId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private Boolean isPaidLeave;

    // Chi tiết đơn
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal days;
    private String reason;
    private String attachmentUrl;

    // Trạng thái & duyệt
    private String status;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
