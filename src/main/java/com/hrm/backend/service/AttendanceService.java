package com.hrm.backend.service;

import com.hrm.backend.dto.AttendanceRequest;
import com.hrm.backend.dto.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    // ==========================================
    // APIs cho Nhân viên (tự chấm công)
    // ==========================================

    /**
     * Nhân viên click button Check-in.
     * Xác định nhân viên qua username (lấy từ JWT token).
     */
    AttendanceResponse checkIn(String username);

    /**
     * Nhân viên click button Check-out.
     * Xác định nhân viên qua username (lấy từ JWT token).
     */
    AttendanceResponse checkOut(String username);

    /**
     * Nhân viên xem trạng thái chấm công ngày hôm nay
     */
    AttendanceResponse getMyToday(String username);

    /**
     * Nhân viên xem lịch sử chấm công của mình theo khoảng ngày
     */
    List<AttendanceResponse> getMyRecords(String username, LocalDate from, LocalDate to);

    // ==========================================
    // APIs cho Admin (quản lý & sửa lỗi)
    // ==========================================

    /**
     * Admin sửa bản ghi chấm công (giờ vào/ra, status, ghi chú).
     * Tự động tính lại workHours và overtimeHours.
     */
    AttendanceResponse adminUpdateRecord(Integer recordId, AttendanceRequest request);

    /**
     * Admin đánh vắng mặt cho 1 nhân viên trong 1 ngày
     */
    AttendanceResponse markAbsent(Integer employeeId, LocalDate date, String note);

    /**
     * Lấy lịch sử chấm công của 1 nhân viên theo khoảng ngày
     */
    List<AttendanceResponse> getRecordsByEmployee(Integer employeeId, LocalDate from, LocalDate to);

    /**
     * Bảng chấm công toàn công ty theo 1 ngày
     */
    List<AttendanceResponse> getRecordsByDate(LocalDate date);

    /**
     * Thống kê chấm công tháng của 1 nhân viên.
     * Module Payroll sẽ gọi method này để lấy totalWorkDays, lateCount, totalOvertimeHours.
     */
    AttendanceResponse.MonthlyStats getMonthlyStats(Integer employeeId, int month, int year);
}
