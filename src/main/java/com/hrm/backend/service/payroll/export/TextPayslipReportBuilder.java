package com.hrm.backend.service.payroll.export;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Concrete Builder dựng báo cáo phiếu lương định dạng Văn bản thuần (Plain Text / Markdown).
 */
public class TextPayslipReportBuilder implements PayslipReportBuilder {
    private final StringBuilder text = new StringBuilder();

    @Override
    public void buildHeader(String companyName, String month) {
        text.append("==================================================\n");
        text.append("             ").append(companyName.toUpperCase()).append("\n");
        text.append("        PHIẾU LƯƠNG NHÂN VIÊN - THÁNG ").append(month).append("\n");
        text.append("==================================================\n");
    }

    @Override
    public void buildEmployeeInfo(String code, String name, String department) {
        text.append("THÔNG TIN NHÂN VIÊN:\n");
        text.append(" - Mã nhân viên: ").append(code).append("\n");
        text.append(" - Họ và tên   : ").append(name).append("\n");
        text.append(" - Phòng ban   : ").append(department != null ? department : "Chưa phân bổ").append("\n");
        text.append("--------------------------------------------------\n");
    }

    @Override
    public void buildSalaryDetails(BigDecimal basicSalary, BigDecimal overtimePay, Map<String, BigDecimal> allowances, BigDecimal grossSalary) {
        text.append("KHOẢN THU NHẬP (EARNINGS):\n");
        text.append(String.format("  [+] Lương cơ bản       : %,.2f VND\n", basicSalary));
        text.append(String.format("  [+] Lương làm thêm giờ : %,.2f VND\n", overtimePay));
        if (allowances != null && !allowances.isEmpty()) {
            for (Map.Entry<String, BigDecimal> entry : allowances.entrySet()) {
                text.append(String.format("  [+] Phụ cấp (%-10s): %,.2f VND\n", entry.getKey(), entry.getValue()));
            }
        }
        text.append(String.format("  ==> TỔNG LƯƠNG GROSS   : %,.2f VND\n", grossSalary));
        text.append("--------------------------------------------------\n");
    }

    @Override
    public void buildDeductions(Map<String, BigDecimal> deductions, BigDecimal totalDeductions) {
        text.append("KHOẢN GIẢM TRỪ (DEDUCTIONS):\n");
        if (deductions != null && !deductions.isEmpty()) {
            for (Map.Entry<String, BigDecimal> entry : deductions.entrySet()) {
                text.append(String.format("  [-] %-18s: %,.2f VND\n", entry.getKey(), entry.getValue()));
            }
        } else {
            text.append("  (Không phát sinh khoản giảm trừ nào)\n");
        }
        text.append(String.format("  ==> TỔNG KHẤU TRỪ      : %,.2f VND\n", totalDeductions));
        text.append("--------------------------------------------------\n");
    }

    @Override
    public void buildNetSalary(BigDecimal netSalary) {
        text.append(">>> THỰC LĨNH (NET SALARY):\n");
        text.append(String.format("    %,.2f VND\n", netSalary));
        text.append("==================================================\n");
    }

    @Override
    public void buildFooter(String note) {
        if (note != null && !note.isBlank()) {
            text.append("Ghi chú: ").append(note).append("\n");
        }
        text.append("Chúc bạn một tháng mới nhiều năng lượng và thành công!\n");
        text.append("==================================================\n");
    }

    @Override
    public PayslipReport getResult() {
        return new PayslipReport("TEXT", text.toString());
    }
}
