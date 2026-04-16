package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.PayrollResponse;
import com.hrm.backend.dto.PayrollUpdateRequest;
import com.hrm.backend.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
@Tag(name = "Payroll Management", description = "API tính lương, duyệt trả lương")
public class PayrollController {

    private final PayrollService payrollService;

    // ========================================
    // KẾ TOÁN: TẠO BẢNG LƯƠNG
    // ========================================

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Tạo bảng lương tháng",
            description = "Tự động tạo bảng lương DRAFT cho tất cả NV ACTIVE. " +
                    "Có thể đính kèm phụ cấp/khấu trừ mặc định áp dụng cho toàn bộ NV.")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> generatePayroll(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "22") BigDecimal workDays,
            @RequestBody(required = false) GenerateRequest body) {

        Map<String, BigDecimal> defaultAllowances = body != null ? body.defaultAllowances : null;
        Map<String, BigDecimal> defaultDeductions = body != null ? body.defaultDeductions : null;

        List<PayrollResponse> results = payrollService.generatePayroll(
                month, year, workDays, defaultAllowances, defaultDeductions);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Đã tạo " + results.size() + " phiếu lương DRAFT tháng " + month + "/" + year,
                        results)
        );
    }

    /** Inner class để nhận body của /generate */
    static class GenerateRequest {
        public Map<String, BigDecimal> defaultAllowances;
        public Map<String, BigDecimal> defaultDeductions;
    }

    // ========================================
    // KẾ TOÁN: SỬA PHIẾU LƯƠNG
    // ========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Sửa phiếu lương",
            description = "Kế toán sửa phụ cấp, khấu trừ, ngày công. Chỉ sửa khi phiếu ở trạng thái DRAFT.")
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayroll(
            @PathVariable Integer id,
            @RequestBody PayrollUpdateRequest request) {

        PayrollResponse response = payrollService.updatePayroll(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Đã cập nhật phiếu lương", response)
        );
    }

    // ========================================
    // KẾ TOÁN: CẬP NHẬT HÀNG LOẠT
    // ========================================

    @PutMapping("/bulk-update")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Cập nhật hàng loạt",
            description = "Áp dụng phụ cấp/khấu trừ cho nhiều phiếu lương cùng lúc (chỉ DRAFT). " +
                    "Phụ cấp/khấu trừ mới sẽ merge vào danh sách hiện có.")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> bulkUpdatePayroll(
            @RequestBody PayrollUpdateRequest.BulkUpdateRequest request) {

        List<PayrollResponse> results = payrollService.bulkUpdatePayroll(request);
        return ResponseEntity.ok(
                ApiResponse.success("Đã cập nhật " + results.size() + " phiếu lương", results)
        );
    }

    // ========================================
    // KẾ TOÁN: CHỐT LƯƠNG
    // ========================================

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Chốt lương",
            description = "Kế toán chốt phiếu lương. Sau khi chốt không thể sửa được nữa (CALCULATED).")
    public ResponseEntity<ApiResponse<PayrollResponse>> submitPayroll(@PathVariable Integer id) {

        PayrollResponse response = payrollService.submitPayroll(id);
        return ResponseEntity.ok(
                ApiResponse.success("Đã chốt phiếu lương", response)
        );
    }

    // ========================================
    // GIÁM ĐỐC: DUYỆT LƯƠNG
    // ========================================

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Duyệt lương",
            description = "Giám đốc duyệt phiếu lương đã chốt (CALCULATED → APPROVED).")
    public ResponseEntity<ApiResponse<PayrollResponse>> approvePayroll(
            Authentication authentication,
            @PathVariable Integer id) {

        String username = authentication.getName();
        PayrollResponse response = payrollService.approvePayroll(username, id);
        return ResponseEntity.ok(
                ApiResponse.success("Đã duyệt phiếu lương", response)
        );
    }

    // ========================================
    // KẾ TOÁN: THANH TOÁN
    // ========================================

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Đánh dấu đã thanh toán",
            description = "Đánh dấu phiếu lương đã được thanh toán (APPROVED → PAID).")
    public ResponseEntity<ApiResponse<PayrollResponse>> markAsPaid(@PathVariable Integer id) {

        PayrollResponse response = payrollService.markAsPaid(id);
        return ResponseEntity.ok(
                ApiResponse.success("Đã đánh dấu thanh toán", response)
        );
    }

    // ========================================
    // XEM BẢNG LƯƠNG
    // ========================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'MANAGER')")
    @Operation(summary = "Bảng lương theo tháng",
            description = "Xem bảng lương toàn công ty theo tháng (format: 2026-04)")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByMonth(
            @RequestParam String month) {

        List<PayrollResponse> responses = payrollService.getPayrollsByMonth(month);
        return ResponseEntity.ok(
                ApiResponse.success("Bảng lương tháng " + month, responses)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'MANAGER')")
    @Operation(summary = "Chi tiết phiếu lương",
            description = "Xem chi tiết 1 phiếu lương")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable Integer id) {

        PayrollResponse response = payrollService.getPayrollById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Chi tiết phiếu lương", response)
        );
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Lịch sử lương nhân viên",
            description = "Xem lịch sử lương của nhân viên (NV chỉ xem được của mình)")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByEmployee(
            @PathVariable Integer employeeId) {

        List<PayrollResponse> responses = payrollService.getPayrollsByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Lịch sử lương nhân viên", responses)
        );
    }
}
