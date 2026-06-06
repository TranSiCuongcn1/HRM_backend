package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Request body cho Kế toán sửa phiếu lương DRAFT
 */
@Builder(toBuilder = true)
public record PayrollUpdateRequest(
    BigDecimal workDays,
    BigDecimal actualDays,
    Map<String, BigDecimal> allowances,
    Map<String, BigDecimal> deductions,
    BigDecimal overtimePay
) {
    /**
     * Inner record cho API Bulk Update: áp dụng allowances/deductions
     * cho nhiều phiếu lương cùng lúc với 1 request.
     */
    @Builder(toBuilder = true)
    public record BulkUpdateRequest(
        List<Integer> payrollIds,
        Map<String, BigDecimal> allowances,
        Map<String, BigDecimal> deductions
    ) {}
}
