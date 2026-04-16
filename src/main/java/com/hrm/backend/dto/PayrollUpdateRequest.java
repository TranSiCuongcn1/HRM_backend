package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Request body cho Kế toán sửa phiếu lương DRAFT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollUpdateRequest {

    /** Sửa ngày công tiêu chuẩn */
    private BigDecimal workDays;

    /** Override ngày công thực tế */
    private BigDecimal actualDays;

    /** Chi tiết phụ cấp: {"Ăn trưa": 500000, "Xăng xe": 300000} */
    private Map<String, BigDecimal> allowances;

    /** Chi tiết khấu trừ: {"BHXH": 1760000, "Thuế TNCN": 500000} */
    private Map<String, BigDecimal> deductions;

    /** Override tiền OT (nếu muốn tính tay thay vì công thức tự động) */
    private BigDecimal overtimePay;

    /**
     * Inner class cho API Bulk Update: áp dụng allowances/deductions
     * cho nhiều phiếu lương cùng lúc với 1 request.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUpdateRequest {
        /** Danh sách payroll ID cần cập nhật */
        private List<Integer> payrollIds;

        /** Phụ cấp áp dụng cho tất cả (merge vào phụ cấp hiện có) */
        private Map<String, BigDecimal> allowances;

        /** Khấu trừ áp dụng cho tất cả (merge vào khấu trừ hiện có) */
        private Map<String, BigDecimal> deductions;
    }
}
