package com.hrm.backend.service;

import com.hrm.backend.dto.LeaveRequestDTO;
import com.hrm.backend.dto.LeaveRequestResponse;

import java.math.BigDecimal;
import java.util.List;

public interface LeaveRequestService {

    /**
     * NV gửi đơn xin phép (validate số dư → tạo PENDING)
     */
    LeaveRequestResponse submitRequest(String username, LeaveRequestDTO request);

    /**
     * NV hủy đơn của chính mình (chỉ khi PENDING)
     */
    LeaveRequestResponse cancelRequest(String username, Integer requestId);

    /**
     * Admin duyệt đơn → APPROVED, tự động trừ số dư phép
     */
    LeaveRequestResponse approveRequest(String adminUsername, Integer requestId);

    /**
     * Admin từ chối → REJECTED
     */
    LeaveRequestResponse rejectRequest(String adminUsername, Integer requestId, String reason);

    /**
     * NV xem danh sách đơn của mình
     */
    List<LeaveRequestResponse> getMyRequests(String username);

    /**
     * Admin xem tất cả đơn đang chờ duyệt
     */
    List<LeaveRequestResponse> getPendingRequests();

    /**
     * Admin xem tất cả đơn (có lọc theo status)
     */
    List<LeaveRequestResponse> getAllRequests(String status);

    /**
     * Payroll gọi: tổng ngày nghỉ có lương đã duyệt trong tháng
     */
    BigDecimal getPaidLeaveDaysInMonth(Integer employeeId, int month, int year);
}
