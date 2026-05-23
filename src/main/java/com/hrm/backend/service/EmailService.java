package com.hrm.backend.service;

import java.math.BigDecimal;
import java.util.Map;

public interface EmailService {
    void sendLeaveStatusEmail(String toEmail, String employeeName, String dateStr, String leaveTypeName, String status, String reason);
    void sendOvertimeStatusEmail(String toEmail, String employeeName, String dateStr, BigDecimal hours, String status, String reason);
    void sendPayslipEmail(String toEmail, String employeeName, String monthStr, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions, BigDecimal netSalary, Map<String, BigDecimal> detailsMap);
    void sendForgotPasswordOtpEmail(String toEmail, String employeeName, String otpCode);
}
