package com.hrm.backend.service.payroll.export;

import com.hrm.backend.dto.PayrollResponse;

/**
 * Lớp Director điều phối các bước dựng báo cáo phiếu lương theo đúng trình tự nghiệp vụ.
 */
public class PayslipReportDirector {
    private final PayslipReportBuilder builder;

    public PayslipReportDirector(PayslipReportBuilder builder) {
        this.builder = builder;
    }

    /**
     * Hàm điều phối việc gọi các hàm dựng của Builder theo trình tự nhất quán.
     */
    public PayslipReport construct(String companyName, PayrollResponse payroll, String note) {
        // 1. Dựng tiêu đề phiếu lương
        builder.buildHeader(companyName, payroll.month());
        
        // 2. Dựng thông tin nhân sự
        builder.buildEmployeeInfo(payroll.employeeCode(), payroll.employeeName(), payroll.departmentName());
        
        // 3. Dựng chi tiết các khoản thu nhập (Lương cứng, tăng ca, phụ cấp)
        builder.buildSalaryDetails(
                payroll.basicSalary(),
                payroll.overtimePay() != null ? payroll.overtimePay() : java.math.BigDecimal.ZERO,
                payroll.allowances(),
                payroll.grossSalary()
        );
        
        // 4. Dựng chi tiết các khoản giảm trừ (Bảo hiểm, thuế TNCN,...)
        builder.buildDeductions(
                payroll.deductions(),
                payroll.totalDeductions()
        );
        
        // 5. Dựng thông tin lương thực nhận (Net)
        builder.buildNetSalary(payroll.netSalary());
        
        // 6. Dựng phần chân trang và ghi chú đi kèm
        builder.buildFooter(note);
        
        // Trả về báo cáo đã được dựng hoàn chỉnh
        return builder.getResult();
    }
}
