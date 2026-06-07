package com.hrm.backend.service.impl;

import com.hrm.backend.service.EmailService;
import com.hrm.backend.service.notification.sender.impl.EmailNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailNotificationSender emailNotificationSender;

    @Async("mailExecutor")
    @Override
    public void sendLeaveStatusEmail(String toEmail, String employeeName, String dateStr, String leaveTypeName, String status, String reason) {
        log.info("Delegating Leave Status Email to {}...", toEmail);
        emailNotificationSender.sendLeaveStatus(toEmail, employeeName, dateStr, leaveTypeName, status, reason);
    }

    @Async("mailExecutor")
    @Override
    public void sendOvertimeStatusEmail(String toEmail, String employeeName, String dateStr, BigDecimal hours, String status, String reason) {
        log.info("Delegating Overtime Status Email to {}...", toEmail);
        emailNotificationSender.sendOvertimeStatus(toEmail, employeeName, dateStr, hours, status, reason);
    }

    @Async("mailExecutor")
    @Override
    public void sendPayslipEmail(String toEmail, String employeeName, String monthStr, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions, BigDecimal netSalary, Map<String, BigDecimal> detailsMap) {
        log.info("Delegating Payslip Email to {}...", toEmail);
        emailNotificationSender.sendPayslip(toEmail, employeeName, monthStr, basicSalary, allowances, deductions, netSalary, detailsMap);
    }

    @Async("mailExecutor")
    @Override
    public void sendForgotPasswordOtpEmail(String toEmail, String employeeName, String otpCode) {
        log.info("Delegating Forgot Password OTP Email to {}...", toEmail);
        emailNotificationSender.sendForgotPasswordOtp(toEmail, employeeName, otpCode);
    }
}
