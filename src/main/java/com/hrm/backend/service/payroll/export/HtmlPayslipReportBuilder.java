package com.hrm.backend.service.payroll.export;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Concrete Builder dựng báo cáo phiếu lương định dạng HTML.
 * Có định dạng tiền tệ chuyên nghiệp.
 */
public class HtmlPayslipReportBuilder implements PayslipReportBuilder {
    private final StringBuilder html = new StringBuilder();

    public HtmlPayslipReportBuilder() {
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<style>\n");
        html.append("  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 30px; color: #333; line-height: 1.6; }\n");
        html.append("  .header { text-align: center; border-bottom: 3px solid #1a73e8; padding-bottom: 15px; margin-bottom: 20px; }\n");
        html.append("  .header h1 { color: #1a73e8; margin: 0; font-size: 24px; text-transform: uppercase; }\n");
        html.append("  .header h2 { color: #5f6368; margin: 5px 0 0 0; font-size: 18px; }\n");
        html.append("  .section { margin-top: 25px; }\n");
        html.append("  .section-title { font-size: 16px; font-weight: bold; border-left: 4px solid #1a73e8; padding-left: 10px; margin-bottom: 10px; color: #1a73e8; }\n");
        html.append("  table { width: 100%; border-collapse: collapse; margin-top: 10px; margin-bottom: 15px; }\n");
        html.append("  th, td { border: 1px solid #dadce0; padding: 10px 12px; text-align: left; font-size: 14px; }\n");
        html.append("  th { background-color: #f8f9fa; color: #3c4043; font-weight: 600; }\n");
        html.append("  .total-row { font-weight: bold; background-color: #e8f0fe; color: #1967d2; }\n");
        html.append("  .net-box { background-color: #e6f4ea; border: 2px solid #137333; padding: 15px; border-radius: 8px; margin-top: 20px; font-size: 18px; font-weight: bold; color: #137333; display: flex; justify-content: space-between; align-items: center; }\n");
        html.append("  .footer { margin-top: 40px; border-top: 1px solid #dadce0; padding-top: 20px; font-style: italic; text-align: center; color: #70757a; font-size: 13px; }\n");
        html.append("</style>\n</head>\n<body>\n");
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,.0f", amount);
    }

    @Override
    public void buildHeader(String companyName, String month) {
        html.append("<div class=\"header\">\n");
        html.append("  <h1>").append(companyName).append("</h1>\n");
        html.append("  <h2>PHIẾU LƯƠNG NHÂN VIÊN (Mẫu HTML) - Tháng ").append(month).append("</h2>\n");
        html.append("</div>\n");
    }

    @Override
    public void buildEmployeeInfo(String code, String name, String department) {
        html.append("<div class=\"section\">\n");
        html.append("  <div class=\"section-title\">Thông tin nhân viên</div>\n");
        html.append("  <table>\n");
        html.append("    <tr>\n");
        html.append("      <td style=\"width: 30%; font-weight: 500; color: #5f6368;\">Mã nhân viên:</td>\n");
        html.append("      <td style=\"font-weight: bold;\">").append(code).append("</td>\n");
        html.append("    </tr>\n");
        html.append("    <tr>\n");
        html.append("      <td style=\"font-weight: 500; color: #5f6368;\">Họ và tên:</td>\n");
        html.append("      <td>").append(name).append("</td>\n");
        html.append("    </tr>\n");
        html.append("    <tr>\n");
        html.append("      <td style=\"font-weight: 500; color: #5f6368;\">Bộ phận/Phòng ban:</td>\n");
        html.append("      <td>").append(department != null ? department : "Chưa phân bổ").append("</td>\n");
        html.append("    </tr>\n");
        html.append("  </table>\n");
        html.append("</div>\n");
    }

    @Override
    public void buildSalaryDetails(BigDecimal basicSalary, BigDecimal overtimePay, Map<String, BigDecimal> allowances, BigDecimal grossSalary) {
        html.append("<div class=\"section\">\n");
        html.append("  <div class=\"section-title\">Chi tiết Thu nhập (Earnings)</div>\n");
        html.append("  <table>\n");
        html.append("    <thead>\n");
        html.append("      <tr><th>Khoản mục</th><th style=\"text-align: right;\">Số tiền (VND)</th></tr>\n");
        html.append("    </thead>\n");
        html.append("    <tbody>\n");
        html.append("      <tr><td>Lương cơ bản (Basic Salary)</td><td style=\"text-align: right;\">").append(formatCurrency(basicSalary)).append("</td></tr>\n");
        html.append("      <tr><td>Lương tăng ca (Overtime Pay)</td><td style=\"text-align: right;\">").append(formatCurrency(overtimePay)).append("</td></tr>\n");
        if (allowances != null && !allowances.isEmpty()) {
            for (Map.Entry<String, BigDecimal> entry : allowances.entrySet()) {
                html.append("      <tr><td>Phụ cấp: ").append(entry.getKey()).append("</td><td style=\"text-align: right;\">").append(formatCurrency(entry.getValue())).append("</td></tr>\n");
            }
        }
        html.append("      <tr class=\"total-row\">\n");
        html.append("        <td>Tổng lương trước giảm trừ (Gross Salary)</td>\n");
        html.append("        <td style=\"text-align: right;\">").append(formatCurrency(grossSalary)).append("</td>\n");
        html.append("      </tr>\n");
        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</div>\n");
    }

    @Override
    public void buildDeductions(Map<String, BigDecimal> deductions, BigDecimal totalDeductions) {
        html.append("<div class=\"section\">\n");
        html.append("  <div class=\"section-title\">Chi tiết Giảm trừ (Deductions)</div>\n");
        html.append("  <table>\n");
        html.append("    <thead>\n");
        html.append("      <tr><th>Khoản mục giảm trừ</th><th style=\"text-align: right;\">Số tiền (VND)</th></tr>\n");
        html.append("    </thead>\n");
        html.append("    <tbody>\n");
        if (deductions != null && !deductions.isEmpty()) {
            for (Map.Entry<String, BigDecimal> entry : deductions.entrySet()) {
                html.append("      <tr><td>").append(entry.getKey()).append("</td><td style=\"text-align: right;\">").append(formatCurrency(entry.getValue())).append("</td></tr>\n");
            }
        } else {
            html.append("      <tr><td colspan=\"2\" style=\"color: #70757a; font-style: italic;\">Không có khoản giảm trừ nào</td></tr>\n");
        }
        html.append("      <tr class=\"total-row\" style=\"background-color: #fce8e6; color: #c5221f;\">\n");
        html.append("        <td>Tổng các khoản giảm trừ (Total Deductions)</td>\n");
        html.append("        <td style=\"text-align: right;\">").append(formatCurrency(totalDeductions)).append("</td>\n");
        html.append("      </tr>\n");
        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</div>\n");
    }

    @Override
    public void buildNetSalary(BigDecimal netSalary) {
        html.append("<div class=\"net-box\">\n");
        html.append("  <span>LƯƠNG THỰC NHẬN (NET SALARY):</span>\n");
        html.append("  <span>").append(formatCurrency(netSalary)).append(" VND</span>\n");
        html.append("</div>\n");
    }

    @Override
    public void buildFooter(String note) {
        html.append("<div class=\"footer\">\n");
        if (note != null && !note.isBlank()) {
            html.append("  <p>Ghi chú: ").append(note).append("</p>\n");
        }
        html.append("  <p>Cảm ơn bạn đã nỗ lực làm việc và đồng hành cùng sự phát triển của công ty!</p>\n");
        html.append("</div>\n");
        html.append("</body>\n</html>");
    }

    @Override
    public PayslipReport getResult() {
        return new PayslipReport("HTML", html.toString());
    }
}
