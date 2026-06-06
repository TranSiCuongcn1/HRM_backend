package com.hrm.backend.service.payroll.export;

import com.hrm.backend.dto.PayrollResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayslipReportExportTest {

    @Test
    @DisplayName("Unit Test GoF Builder - Dựng phiếu lương định dạng HTML thông qua Director")
    void testHtmlPayslipExport() {
        // 1. Chuẩn bị dữ liệu đầu vào (PayrollResponse DTO)
        Map<String, BigDecimal> allowances = new LinkedHashMap<>();
        allowances.put("Phụ cấp ăn trưa", new BigDecimal("1000000"));
        allowances.put("Phụ cấp xăng xe", new BigDecimal("500000"));

        Map<String, BigDecimal> deductions = new LinkedHashMap<>();
        deductions.put("BHXH (8%)", new BigDecimal("800000"));
        deductions.put("Thuế TNCN", new BigDecimal("250000"));

        PayrollResponse payroll = PayrollResponse.builder()
                .employeeCode("EMP0001")
                .employeeName("Nguyễn Văn A")
                .departmentName("Phòng Công Nghệ")
                .month("2026-06")
                .basicSalary(new BigDecimal("15000000"))
                .overtimePay(new BigDecimal("1500000"))
                .allowances(allowances)
                .grossSalary(new BigDecimal("17000000"))
                .deductions(deductions)
                .totalDeductions(new BigDecimal("1050000"))
                .netSalary(new BigDecimal("15950000"))
                .build();

        // 2. Sử dụng Html Builder và Director để dựng
        PayslipReportBuilder builder = new HtmlPayslipReportBuilder();
        PayslipReportDirector director = new PayslipReportDirector(builder);

        PayslipReport report = director.construct("Công Ty Cổ Phần Antigravity", payroll, "Kỳ thanh toán lương đúng hạn.");

        // 3. Xác minh kết quả dựng báo cáo HTML
        assertThat(report.getFormat()).isEqualTo("HTML");
        assertThat(report.getContent()).contains("<!DOCTYPE html>");
        assertThat(report.getContent()).contains("Công Ty Cổ Phần Antigravity");
        assertThat(report.getContent()).contains("EMP0001");
        assertThat(report.getContent()).contains("Nguyễn Văn A");
        assertThat(report.getContent()).contains("Phòng Công Nghệ");
        assertThat(report.getContent()).contains("15,000,000"); // Hoặc dạng text thô
        assertThat(report.getContent()).contains("15,950,000 VND");
    }

    @Test
    @DisplayName("Unit Test GoF Builder - Dựng phiếu lương định dạng Text/Markdown thông qua Director")
    void testTextPayslipExport() {
        // 1. Chuẩn bị dữ liệu
        Map<String, BigDecimal> allowances = new LinkedHashMap<>();
        allowances.put("Ăn trưa", new BigDecimal("1000000"));

        Map<String, BigDecimal> deductions = new LinkedHashMap<>();
        deductions.put("BHXH", new BigDecimal("800000"));

        PayrollResponse payroll = PayrollResponse.builder()
                .employeeCode("EMP0002")
                .employeeName("Trần Thị B")
                .departmentName("Phòng Nhân Sự")
                .month("2026-06")
                .basicSalary(new BigDecimal("12000000"))
                .overtimePay(BigDecimal.ZERO)
                .allowances(allowances)
                .grossSalary(new BigDecimal("13000000"))
                .deductions(deductions)
                .totalDeductions(new BigDecimal("800000"))
                .netSalary(new BigDecimal("12200000"))
                .build();

        // 2. Sử dụng Text Builder và Director để dựng
        PayslipReportBuilder builder = new TextPayslipReportBuilder();
        PayslipReportDirector director = new PayslipReportDirector(builder);

        PayslipReport report = director.construct("Công Ty Cổ Phần Antigravity", payroll, "Lương chuyển khoản ngân hàng.");

        // 3. Xác minh kết quả dựng báo cáo TEXT
        assertThat(report.getFormat()).isEqualTo("TEXT");
        assertThat(report.getContent()).contains("==================================================");
        assertThat(report.getContent()).contains("EMP0002");
        assertThat(report.getContent()).contains("Trần Thị B");
        assertThat(report.getContent()).contains("12,200,000.00 VND");
    }
}
