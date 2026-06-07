package com.hrm.backend.service.payroll.export;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface Builder định nghĩa các bộ phận/công đoạn cần lắp ghép của một Phiếu lương.
 */
public interface PayslipReportBuilder {
    
    void buildHeader(String companyName, String month);
    
    void buildEmployeeInfo(String code, String name, String department);
    
    void buildSalaryDetails(BigDecimal basicSalary, BigDecimal overtimePay, Map<String, BigDecimal> allowances, BigDecimal grossSalary);
    
    void buildDeductions(Map<String, BigDecimal> deductions, BigDecimal totalDeductions);
    
    void buildNetSalary(BigDecimal netSalary);
    
    void buildFooter(String note);
    
    PayslipReport getResult();
}
