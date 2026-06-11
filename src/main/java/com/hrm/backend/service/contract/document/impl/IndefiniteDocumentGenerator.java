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
public class IndefiniteDocumentGenerator implements ContractDocumentGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String generateSummary(String employeeName, String contractType,
                                   LocalDate startDate, LocalDate endDate,
                                   BigDecimal basicSalary) {
        String startStr = startDate != null ? startDate.format(DATE_FORMATTER) : "N/A";
        BigDecimal effectiveSalary = basicSalary != null ? basicSalary.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return String.format(
                "<div style=\"font-family: 'Segoe UI', Roboto, sans-serif; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; background-color: #ffffff; max-width: 600px; margin: 10px 0; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">" +
                "  <h3 style=\"color: #065f46; margin-top: 0; font-size: 16px; border-bottom: 2px solid #059669; padding-bottom: 8px;\">HỢP ĐỒNG KHÔNG XÁC ĐỊNH THỜI HẠN</h3>" +
                "  <table style=\"width: 100%%; border-collapse: collapse; margin: 15px 0; font-size: 14px;\">" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b; width: 40%%;\">Nhân viên:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">%s</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Loại hợp đồng:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">Không xác định thời hạn</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Thời gian:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">Từ %s (Không xác định thời hạn kết thúc)</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Lương cơ bản:</td><td style=\"padding: 6px 0; color: #1e293b; font-weight: 600;\">%s</td></tr>" +
                "    <tr><td style=\"padding: 6px 0; color: #64748b;\">Lương thực hưởng (100%%):</td><td style=\"padding: 6px 0; color: #065f46; font-weight: 700; font-size: 15px;\">%s</td></tr>" +
                "  </table>" +
                "  <div style=\"background-color: #ecfdf5; border-left: 4px solid #10b981; padding: 12px; border-radius: 6px; font-size: 13px; color: #065f46; line-height: 1.5;\">" +
                "    <strong>🍀 Quy định lao động:</strong> Đây là hợp đồng lao động không xác định thời hạn, mang lại sự ràng buộc bền vững và quyền lợi lâu dài nhất cho người lao động theo pháp luật Việt Nam." +
                "  </div>" +
                "</div>",
                employeeName, startStr, formatVND(basicSalary), formatVND(effectiveSalary)
        );
    }

    @Override
    public String getContractTitle() {
        return "HỢP ĐỒNG KHÔNG XÁC ĐỊNH THỜI HẠN";
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VNĐ";
    }
}
