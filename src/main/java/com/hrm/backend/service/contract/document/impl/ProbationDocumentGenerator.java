package com.hrm.backend.service.contract.document.impl;

import com.hrm.backend.service.contract.document.ContractDocumentGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class ProbationDocumentGenerator implements ContractDocumentGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String generateSummary(String employeeName, String contractType,
                                   LocalDate startDate, LocalDate endDate,
                                   BigDecimal basicSalary) {
        String startStr = startDate != null ? startDate.format(DATE_FORMATTER) : "N/A";
        String endStr = endDate != null ? endDate.format(DATE_FORMATTER) : "N/A";
        BigDecimal effectiveSalary = basicSalary != null ? basicSalary.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return String.format(
                "<div style=\"font-family: 'Segoe UI', Roboto, sans-serif; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; background-color: #ffffff; max-width: 600px; margin: 10px 0; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">" +
                "  <h3 style=\"color: #b45309; margin-top: 0; font-size: 16px; border-bottom: 2px solid #f59e0b; padding-bottom: 8px;\">HỢP ĐỒNG THỬ VIỆC</h3>" +
                "  <table style=\"width: 100%%; border-collapse: collapse; margin: 15px 0; font-size: 14px;\">" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b; width: 40%%;\">Nhân viên:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">%s</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Loại hợp đồng:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">Thử việc</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Thời gian:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">Từ %s đến %s</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Lương cơ bản:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">%s</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Lương thực hưởng (85%%):</td><td style=\"padding: 6px 0; color: #b45309; font-weight: 700; font-size: 15px;\">%s</td></tr>" +
                "  </table>" +
                "  <div style=\"background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 12px; border-radius: 6px; font-size: 13px; color: #b45309; line-height: 1.5;\">" +
                "    <strong>⚠️ Quy định lao động:</strong> Thời gian thử việc tối đa là 180 ngày và lương thử việc phải đạt ít nhất 85%% mức lương chính thức (Theo Điều 26 Bộ luật Lao động 2019)." +
                "  </div>" +
                "</div>",
                employeeName, startStr, endStr, formatVND(basicSalary), formatVND(effectiveSalary)
        );
    }

    @Override
    public String getContractTitle() {
        return "HỢP ĐỒNG THỬ VIỆC";
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VNĐ";
    }
}
