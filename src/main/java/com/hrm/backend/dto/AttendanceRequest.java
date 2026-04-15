package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request body cho Admin khi sửa lỗi chấm công
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    private Integer employeeId;

    private LocalDate date;

    private LocalTime checkIn;

    private LocalTime checkOut;

    /** Admin override trạng thái (ON_TIME, LATE, EARLY_LEAVE, ABSENT, HALF_DAY) */
    private String status;

    /** Lý do sửa / ghi chú */
    private String note;
}
