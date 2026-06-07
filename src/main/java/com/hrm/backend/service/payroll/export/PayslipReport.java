package com.hrm.backend.service.payroll.export;

/**
 * Lớp sản phẩm cuối cùng (Product) đại diện cho Báo cáo Phiếu lương.
 * Chứa nội dung đã được dựng hoàn chỉnh theo định dạng tương ứng.
 */
public class PayslipReport {
    private final String format;
    private final String content;

    public PayslipReport(String format, String content) {
        this.format = format;
        this.content = content;
    }

    public String getFormat() {
        return format;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "PayslipReport{" +
                "format='" + format + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
