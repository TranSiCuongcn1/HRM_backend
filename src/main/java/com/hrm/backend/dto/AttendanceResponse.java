package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder(toBuilder = true)
public record AttendanceResponse(
    Integer id,
    Integer employeeId,
    String employeeCode,
    String employeeName,
    LocalDate date,
    LocalTime checkIn,
    LocalTime checkOut,
    String status,
    BigDecimal overtimeHours,
    BigDecimal workHours,
    Integer lateMinutes,
    Integer earlyLeaveMinutes,
    String note,
    String checkInIp,
    BigDecimal checkInLat,
    BigDecimal checkInLng,
    String checkOutIp,
    BigDecimal checkOutLat,
    BigDecimal checkOutLng,
    Boolean checkInGpsValid,
    Boolean checkInIpValid,
    Boolean checkOutGpsValid,
    Boolean checkOutIpValid,
    LocalDateTime createdAt
) {
    /**
     * DTO phụ: Thống kê chấm công theo tháng (cho Payroll sử dụng)
     */
    @Builder
    public record MonthlyStats(
        Integer employeeId,
        String employeeCode,
        String employeeName,
        int month,
        int year,
        long totalWorkDays,      // Số ngày có đi làm (không tính ABSENT)
        long lateCount,          // Số lần đi trễ
        BigDecimal totalOvertimeHours // Tổng giờ OT trong tháng
    ) {}
}
