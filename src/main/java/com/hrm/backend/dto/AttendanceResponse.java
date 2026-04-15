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
public class AttendanceResponse {

    private Integer id;
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String status;
    private BigDecimal overtimeHours;
    private BigDecimal workHours;
    private String note;
    private LocalDateTime createdAt;

    /**
     * DTO phụ: Thống kê chấm công theo tháng (cho Payroll sử dụng)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyStats {
        private Integer employeeId;
        private String employeeCode;
        private String employeeName;
        private int month;
        private int year;
        private long totalWorkDays;      // Số ngày có đi làm (không tính ABSENT)
        private long lateCount;          // Số lần đi trễ
        private BigDecimal totalOvertimeHours; // Tổng giờ OT trong tháng
    }
}
