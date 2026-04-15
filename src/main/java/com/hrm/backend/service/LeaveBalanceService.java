package com.hrm.backend.service;

import com.hrm.backend.dto.LeaveBalanceResponse;

import java.math.BigDecimal;
import java.util.List;

public interface LeaveBalanceService {

    /**
     * Cấp số ngày phép mặc định cho NV đầu năm (Admin gọi)
     */
    void initBalanceForEmployee(Integer employeeId, int year);

    /**
     * Xem tất cả số dư phép của NV trong 1 năm
     */
    List<LeaveBalanceResponse> getBalancesByEmployee(Integer employeeId, int year);

    /**
     * Admin cập nhật số phép thủ công
     */
    LeaveBalanceResponse updateBalance(Integer balanceId, BigDecimal totalDays, BigDecimal carryOverDays);

    /**
     * Trừ phép khi đơn được duyệt (internal, gọi từ LeaveRequestService)
     */
    void deductBalance(Integer employeeId, Integer leaveTypeId, int year, BigDecimal days);

    /**
     * Hoàn phép (nếu cần trong tương lai)
     */
    void refundBalance(Integer employeeId, Integer leaveTypeId, int year, BigDecimal days);
}
