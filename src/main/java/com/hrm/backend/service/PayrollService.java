package com.hrm.backend.service;

import com.hrm.backend.dto.PayrollResponse;
import com.hrm.backend.dto.PayrollUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PayrollService {

    /**
     * Tự động tạo bảng lương DRAFT cho tất cả NV ACTIVE trong tháng.
     * Kế toán có thể đính kèm phụ cấp/khấu trừ mặc định sẵn khi generate.
     *
     * @param month     Tháng (1-12)
     * @param year      Năm (2026)
     * @param workDays  Ngày công tiêu chuẩn (mặc định 22)
     * @param defaultAllowances Phụ cấp mặc định áp dụng cho tất cả NV (nullable)
     * @param defaultDeductions Khấu trừ mặc định áp dụng cho tất cả NV (nullable)
     */
    List<PayrollResponse> generatePayroll(int month, int year, BigDecimal workDays,
                                          Map<String, BigDecimal> defaultAllowances,
                                          Map<String, BigDecimal> defaultDeductions);

    /**
     * Kế toán sửa phiếu lương DRAFT (phụ cấp, khấu trừ, ngày công).
     * Tự động tính lại grossSalary và netSalary.
     */
    PayrollResponse updatePayroll(Integer payrollId, PayrollUpdateRequest request);

    /**
     * Bulk update: áp dụng phụ cấp/khấu trừ cho nhiều phiếu lương cùng lúc.
     * Chỉ áp dụng cho phiếu có status = DRAFT.
     */
    List<PayrollResponse> bulkUpdatePayroll(PayrollUpdateRequest.BulkUpdateRequest request);

    /**
     * Kế toán chốt lương → CALCULATED (khóa sửa)
     */
    PayrollResponse submitPayroll(Integer payrollId);

    /**
     * Giám đốc duyệt → APPROVED
     */
    PayrollResponse approvePayroll(String approverUsername, Integer payrollId);

    /**
     * Đánh dấu đã thanh toán → PAID
     */
    PayrollResponse markAsPaid(Integer payrollId);

    /**
     * Bảng lương toàn công ty theo tháng
     */
    Page<PayrollResponse> getPayrollsByMonth(String month, String status, String keyword, Integer departmentId, Pageable pageable);

    /**
     * Lịch sử lương của NV
     */
    Page<PayrollResponse> getPayrollsByEmployee(Integer employeeId, String status, Pageable pageable);

    /**
     * Chi tiết 1 phiếu lương
     */
    PayrollResponse getPayrollById(Integer id);
}
