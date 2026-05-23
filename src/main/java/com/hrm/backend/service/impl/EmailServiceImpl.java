package com.hrm.backend.service.impl;

import com.hrm.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private static final String COMPANY_NAME = "Hệ thống Quản lý Nhân sự HRM";
    private static final String BRAND_COLOR = "#3d6b59";

    @Async("mailExecutor")
    @Override
    public void sendLeaveStatusEmail(String toEmail, String employeeName, String dateStr, String leaveTypeName, String status, String reason) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Cannot send email: recipient address is empty.");
            return;
        }

        try {
            log.info("Sending Leave Status Email to {} (async)...", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[" + COMPANY_NAME + "] Thông báo kết quả đơn xin nghỉ phép");

            String statusLabel = "Đã phê duyệt";
            String statusBadgeColor = "#e6f4ea";
            String statusTextColor = "#137333";

            if ("REJECTED".equalsIgnoreCase(status)) {
                statusLabel = "Từ chối";
                statusBadgeColor = "#fce8e6";
                statusTextColor = "#c5221f";
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                statusLabel = "Đã hủy";
                statusBadgeColor = "#f1f3f4";
                statusTextColor = "#5f6368";
            }

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>")
                    .append("<body style='margin:0;padding:0;background-color:#f8fafc;font-family:\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;'>")
                    .append("<div style='max-width:600px;margin:30px auto;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,0.05);border:1px solid #e2e8f0;'>")
                    // Header
                    .append("<div style='background-color:").append(BRAND_COLOR).append(";padding:24px;text-align:center;'>")
                    .append("<h2 style='color:#ffffff;margin:0;font-size:22px;letter-spacing:1px;font-weight:700;'>").append(COMPANY_NAME.toUpperCase()).append("</h2>")
                    .append("</div>")
                    // Content
                    .append("<div style='padding:32px;color:#334155;line-height:1.6;'>")
                    .append("<p style='font-size:16px;margin-top:0;'>Kính gửi anh/chị <strong>").append(employeeName).append("</strong>,</p>")
                    .append("<p style='font-size:15px;'>Yêu cầu xin nghỉ phép của anh/chị đã được cập nhật bởi Bộ phận Nhân sự. Chi tiết trạng thái như sau:</p>")
                    
                    // Card details
                    .append("<div style='background-color:#f1f5f9;border-radius:12px;padding:20px;margin:24px 0;'>")
                    .append("<table style='width:100%;border-collapse:collapse;'>")
                    .append("<tr><td style='padding:8px 0;color:#64748b;font-weight:600;width:130px;font-size:14px;'>Loại nghỉ phép:</td>")
                    .append("<td style='padding:8px 0;color:#1e293b;font-weight:700;font-size:14px;'>").append(leaveTypeName).append("</td></tr>")
                    .append("<tr><td style='padding:8px 0;color:#64748b;font-weight:600;font-size:14px;'>Thời gian nghỉ:</td>")
                    .append("<td style='padding:8px 0;color:#1e293b;font-weight:700;font-size:14px;'>").append(dateStr).append("</td></tr>")
                    .append("<tr><td style='padding:8px 0;color:#64748b;font-weight:600;font-size:14px;'>Trạng thái:</td>")
                    .append("<td style='padding:8px 0;'>")
                    .append("<span style='background-color:").append(statusBadgeColor).append(";color:").append(statusTextColor)
                    .append(";padding:6px 14px;border-radius:20px;font-weight:700;font-size:13px;display:inline-block;'>")
                    .append(statusLabel).append("</span>")
                    .append("</td></tr>");

            if ("REJECTED".equalsIgnoreCase(status) && reason != null && !reason.trim().isEmpty()) {
                htmlContent.append("<tr><td style='padding:8px 0;color:#c5221f;font-weight:600;font-size:14px;'>Lý do từ chối:</td>")
                        .append("<td style='padding:8px 0;color:#c5221f;font-weight:600;font-size:14px;'>").append(reason).append("</td></tr>");
            }

            htmlContent.append("</table>")
                    .append("</div>")

                    .append("<p style='font-size:15px;margin-bottom:0;'>Nếu có bất kỳ thắc mắc nào, anh/chị vui lòng liên hệ trực tiếp phòng Nhân sự để được hỗ trợ.</p>")
                    .append("<p style='font-size:15px;'>Trân trọng cảm ơn,</p>")
                    .append("<p style='font-size:15px;color:").append(BRAND_COLOR).append(";font-weight:700;margin-top:4px;'>Ban Quản Trị Nhân Sự</p>")
                    .append("</div>")
                    // Footer
                    .append("<div style='background-color:#f8fafc;padding:20px;text-align:center;border-top:1px solid #e2e8f0;color:#94a3b8;font-size:12px;'>")
                    .append("<p style='margin:0;'>Đây là email tự động được gửi từ hệ thống ").append(COMPANY_NAME).append(".</p>")
                    .append("<p style='margin:4px 0 0 0;'>Vui lòng không trả lời (reply) trực tiếp vào email này.</p>")
                    .append("</div>")
                    .append("</div></body></html>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(message);
            log.info("Leave Status Email successfully sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send Leave Status Email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async("mailExecutor")
    @Override
    public void sendOvertimeStatusEmail(String toEmail, String employeeName, String dateStr, BigDecimal hours, String status, String reason) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Cannot send email: recipient address is empty.");
            return;
        }

        try {
            log.info("Sending Overtime Status Email to {} (async)...", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[" + COMPANY_NAME + "] Thông báo kết quả đơn đăng ký tăng ca");

            String statusLabel = "Đã phê duyệt";
            String statusBadgeColor = "#e6f4ea";
            String statusTextColor = "#137333";

            if ("REJECTED".equalsIgnoreCase(status)) {
                statusLabel = "Từ chối";
                statusBadgeColor = "#fce8e6";
                statusTextColor = "#c5221f";
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                statusLabel = "Đã hủy";
                statusBadgeColor = "#f1f3f4";
                statusTextColor = "#5f6368";
            }

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>")
                    .append("<body style='margin:0;padding:0;background-color:#f8fafc;font-family:\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;'>")
                    .append("<div style='max-width:600px;margin:30px auto;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,0.05);border:1px solid #e2e8f0;'>")
                    // Header
                    .append("<div style='background-color:").append(BRAND_COLOR).append(";padding:24px;text-align:center;'>")
                    .append("<h2 style='color:#ffffff;margin:0;font-size:22px;letter-spacing:1px;font-weight:700;'>").append(COMPANY_NAME.toUpperCase()).append("</h2>")
                    .append("</div>")
                    // Content
                    .append("<div style='padding:32px;color:#334155;line-height:1.6;'>")
                    .append("<p style='font-size:16px;margin-top:0;'>Kính gửi anh/chị <strong>").append(employeeName).append("</strong>,</p>")
                    .append("<p style='font-size:15px;'>Đơn đăng ký tăng ca (OT) của anh/chị đã được cập nhật bởi Ban Quản Lý. Chi tiết như sau:</p>")
                    
                    // Card details
                    .append("<div style='background-color:#f1f5f9;border-radius:12px;padding:20px;margin:24px 0;'>")
                    .append("<table style='width:100%;border-collapse:collapse;'>")
                    .append("<tr><td style='padding:8px 0;color:#64748b;font-weight:600;width:130px;font-size:14px;'>Ngày tăng ca:</td>")
                    .append("<td style='padding:8px 0;color:#1e293b;font-weight:700;font-size:14px;'>").append(dateStr).append("</td></tr>")
                    .append("<tr><td style='padding:8px 0;color:#64748b;font-weight:600;font-size:14px;'>Số giờ đăng ký:</td>")
                    .append("<td style='padding:8px 0;color:#1e293b;font-weight:700;font-size:14px;'>").append(hours).append(" giờ</td></tr>")
                    .append("<tr><td style='padding:8px 0;color:#64748b;font-weight:600;font-size:14px;'>Trạng thái:</td>")
                    .append("<td style='padding:8px 0;'>")
                    .append("<span style='background-color:").append(statusBadgeColor).append(";color:").append(statusTextColor)
                    .append(";padding:6px 14px;border-radius:20px;font-weight:700;font-size:13px;display:inline-block;'>")
                    .append(statusLabel).append("</span>")
                    .append("</td></tr>");

            if ("REJECTED".equalsIgnoreCase(status) && reason != null && !reason.trim().isEmpty()) {
                htmlContent.append("<tr><td style='padding:8px 0;color:#c5221f;font-weight:600;font-size:14px;'>Lý do từ chối:</td>")
                        .append("<td style='padding:8px 0;color:#c5221f;font-weight:600;font-size:14px;'>").append(reason).append("</td></tr>");
            }

            htmlContent.append("</table>")
                    .append("</div>")

                    .append("<p style='font-size:15px;margin-bottom:0;'>Nếu có bất kỳ thắc mắc nào, anh/chị vui lòng liên hệ phòng Nhân sự.</p>")
                    .append("<p style='font-size:15px;'>Trân trọng cảm ơn,</p>")
                    .append("<p style='font-size:15px;color:").append(BRAND_COLOR).append(";font-weight:700;margin-top:4px;'>Ban Quản Trị Nhân Sự</p>")
                    .append("</div>")
                    // Footer
                    .append("<div style='background-color:#f8fafc;padding:20px;text-align:center;border-top:1px solid #e2e8f0;color:#94a3b8;font-size:12px;'>")
                    .append("<p style='margin:0;'>Đây là email tự động được gửi từ hệ thống ").append(COMPANY_NAME).append(".</p>")
                    .append("<p style='margin:4px 0 0 0;'>Vui lòng không trả lời trực tiếp vào email này.</p>")
                    .append("</div>")
                    .append("</div></body></html>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(message);
            log.info("Overtime Status Email successfully sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send Overtime Status Email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async("mailExecutor")
    @Override
    public void sendPayslipEmail(String toEmail, String employeeName, String monthStr, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions, BigDecimal netSalary, Map<String, BigDecimal> detailsMap) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Cannot send email: recipient address is empty.");
            return;
        }

        try {
            log.info("Sending Payslip Email to {} (async)...", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[" + COMPANY_NAME + "] Phiếu lương tháng " + monthStr);

            String formattedBasic = formatVND(basicSalary);
            String formattedAllowances = formatVND(allowances);
            String formattedDeductions = formatVND(deductions);
            String formattedNet = formatVND(netSalary);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>")
                    .append("<body style='margin:0;padding:0;background-color:#f8fafc;font-family:\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;'>")
                    .append("<div style='max-width:650px;margin:30px auto;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.06);border:1px solid #e2e8f0;'>")
                    // Header
                    .append("<div style='background-color:").append(BRAND_COLOR).append(";padding:28px;text-align:center;'>")
                    .append("<h2 style='color:#ffffff;margin:0;font-size:24px;letter-spacing:1px;font-weight:700;'>").append(COMPANY_NAME.toUpperCase()).append("</h2>")
                    .append("<p style='color:#cbd5e1;margin:6px 0 0 0;font-size:14px;font-weight:500;'>PHIẾU BÁO LƯƠNG CHI TIẾT - THÁNG ").append(monthStr).append("</p>")
                    .append("</div>")
                    // Content
                    .append("<div style='padding:32px;color:#334155;line-height:1.6;'>")
                    .append("<p style='font-size:16px;margin-top:0;'>Kính gửi anh/chị <strong>").append(employeeName).append("</strong>,</p>")
                    .append("<p style='font-size:15px;'>Ban Nhân Sự xin gửi bảng báo lương chi tiết của anh/chị trong tháng <strong>").append(monthStr).append("</strong> như sau:</p>")
                    
                    // Main Net Salary Panel
                    .append("<div style='background-color:#e6f4ea;border-left:5px solid #137333;border-radius:8px;padding:18px 24px;margin:24px 0;display:flex;justify-content:between;align-items:center;'>")
                    .append("<div>")
                    .append("<span style='color:#137333;font-size:14px;font-weight:700;text-transform:uppercase;'>Thực Nhận (Net Salary)</span><br/>")
                    .append("<span style='color:#137333;font-size:28px;font-weight:800;'>").append(formattedNet).append("</span>")
                    .append("</div>")
                    .append("</div>")

                    // Salary breakdown table
                    .append("<h3 style='font-size:16px;color:#1e293b;border-bottom:2px solid #f1f5f9;padding-bottom:8px;margin-bottom:12px;font-weight:700;'>KHOẢN THU NHẬP & PHỤ CẤP</h3>")
                    .append("<table style='width:100%;border-collapse:collapse;margin-bottom:24px;'>")
                    .append("<tr style='border-bottom:1px solid #f1f5f9;'><td style='padding:10px 0;color:#64748b;font-size:14px;'>Lương cơ bản (Contract Salary)</td><td style='padding:10px 0;text-align:right;font-weight:700;color:#1e293b;font-size:14px;'>").append(formattedBasic).append("</td></tr>")
                    .append("<tr style='border-bottom:1px solid #f1f5f9;'><td style='padding:10px 0;color:#64748b;font-size:14px;'>Tổng các khoản phụ cấp nhận thêm</td><td style='padding:10px 0;text-align:right;font-weight:700;color:#137333;font-size:14px;'>+ ").append(formattedAllowances).append("</td></tr>")
                    .append("</table>");

            // Deductions breakdown table
            htmlContent.append("<h3 style='font-size:16px;color:#1e293b;border-bottom:2px solid #f1f5f9;padding-bottom:8px;margin-bottom:12px;font-weight:700;'>KHOẢN KHẤU TRỪ / NGHĨA VỤ</h3>")
                    .append("<table style='width:100%;border-collapse:collapse;margin-bottom:24px;'>");

            if (detailsMap != null && !detailsMap.isEmpty()) {
                for (Map.Entry<String, BigDecimal> entry : detailsMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                        htmlContent.append("<tr style='border-bottom:1px solid #f1f5f9;'>")
                                .append("<td style='padding:10px 0;color:#64748b;font-size:14px;'>").append(entry.getKey()).append("</td>")
                                .append("<td style='padding:10px 0;text-align:right;font-weight:700;color:#c5221f;font-size:14px;'>- ").append(formatVND(entry.getValue())).append("</td>")
                                .append("</tr>");
                    }
                }
            } else {
                htmlContent.append("<tr style='border-bottom:1px solid #f1f5f9;'><td style='padding:10px 0;color:#64748b;font-size:14px;'>Tổng các khoản khấu trừ</td><td style='padding:10px 0;text-align:right;font-weight:700;color:#c5221f;font-size:14px;'>- ").append(formattedDeductions).append("</td></tr>");
            }
            
            htmlContent.append("</table>")

                    .append("<p style='font-size:14px;color:#64748b;font-style:italic;background-color:#f8fafc;padding:12px 16px;border-radius:8px;border:1px solid #f1f5f9;'>")
                    .append("(*) Ghi chú: Số liệu chi tiết trên dựa trên ngày công chấm công và hợp đồng lao động đã ký kết. Mọi thắc mắc về số liệu vui lòng liên hệ phòng Kế toán/Nhân sự trước ngày 05 hàng tháng.")
                    .append("</p>")

                    .append("<p style='font-size:15px;margin-top:24px;'>Trân trọng,</p>")
                    .append("<p style='font-size:15px;color:").append(BRAND_COLOR).append(";font-weight:700;margin-top:4px;'>Ban Quản Trị Nhân Sự & Tài Chính</p>")
                    .append("</div>")
                    // Footer
                    .append("<div style='background-color:#f8fafc;padding:20px;text-align:center;border-top:1px solid #e2e8f0;color:#94a3b8;font-size:12px;'>")
                    .append("<p style='margin:0;'>Đây là email tự động gửi từ ").append(COMPANY_NAME).append(".</p>")
                    .append("<p style='margin:4px 0 0 0;'>Bảo mật thông tin: Nội dung email này chứa thông tin cá nhân bảo mật, vui lòng không chia sẻ ra ngoài.</p>")
                    .append("</div>")
                    .append("</div></body></html>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(message);
            log.info("Payslip Email successfully sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send Payslip Email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        try {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(amount) + " VNĐ";
        } catch (Exception e) {
            return String.format("%,.0f VNĐ", amount);
        }
    }

    @Async("mailExecutor")
    @Override
    public void sendForgotPasswordOtpEmail(String toEmail, String employeeName, String otpCode) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("Cannot send email: recipient address is empty.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔑 Xác Nhận Khôi Phục Mật Khẩu - " + COMPANY_NAME);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head>")
                    .append("<meta charset='UTF-8'>")
                    .append("<style>")
                    .append("body { font-family: 'Inter', sans-serif; background-color: #f1f5f9; margin: 0; padding: 20px; color: #1e293b; }")
                    .append("</style>")
                    .append("</head><body>")
                    .append("<div style='max-width:600px;margin:0 auto;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 15px -3px rgba(0,0,0,0.1);border:1px solid #e2e8f0;'>")
                    // Header
                    .append("<div style='background-color:").append(BRAND_COLOR).append(";padding:30px;text-align:center;'>")
                    .append("<h1 style='color:#ffffff;margin:0;font-size:24px;font-weight:700;text-transform:uppercase;letter-spacing:1px;'>Khôi Phục Mật Khẩu</h1>")
                    .append("</div>")
                    // Body
                    .append("<div style='padding:30px 40px;'>")
                    .append("<p style='font-size:16px;font-weight:700;margin-top:0;'>Xin chào ").append(employeeName != null ? employeeName : "Thành viên").append(",</p>")
                    .append("<p style='font-size:15px;line-height:1.6;'>Chúng tôi nhận được yêu cầu thay đổi mật khẩu cho tài khoản đăng nhập liên kết với email này.</p>")
                    .append("<p style='font-size:15px;line-height:1.6;'>Vui lòng sử dụng mã xác thực OTP dưới đây để hoàn tất việc thiết lập mật khẩu mới của bạn:</p>")
                    
                    // OTP Box
                    .append("<div style='background-color:#f8fafc;border:2px dashed ").append(BRAND_COLOR).append(";border-radius:12px;padding:24px;text-align:center;margin:24px 0;'>")
                    .append("<span style='font-size:36px;font-weight:800;letter-spacing:8px;color:").append(BRAND_COLOR).append(";font-family:monospace;'>").append(otpCode).append("</span>")
                    .append("</div>")
                    
                    .append("<p style='font-size:14px;color:#c5221f;font-weight:600;background-color:#fdf2f2;padding:12px 16px;border-radius:8px;'>")
                    .append("⚠️ Cảnh báo bảo mật: Mã OTP này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này với bất kỳ ai để phòng tránh mất tài khoản.")
                    .append("</p>")
                    
                    .append("<p style='font-size:15px;line-height:1.6;margin-top:20px;'>Nếu không phải bạn thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc đổi mật khẩu ngay lập tức để giữ an toàn cho tài khoản.</p>")
                    .append("<p style='font-size:15px;margin-top:30px;margin-bottom:0;'>Trân trọng,</p>")
                    .append("<p style='font-size:15px;color:").append(BRAND_COLOR).append(";font-weight:700;margin-top:4px;'>Ban Quản Trị Nhân Sự</p>")
                    .append("</div>")
                    // Footer
                    .append("<div style='background-color:#f8fafc;padding:20px;text-align:center;border-top:1px solid #e2e8f0;color:#94a3b8;font-size:12px;'>")
                    .append("<p style='margin:0;'>Đây là email tự động gửi từ ").append(COMPANY_NAME).append(".</p>")
                    .append("<p style='margin:4px 0 0 0;'>Vui lòng không phản hồi trực tiếp vào địa chỉ thư này.</p>")
                    .append("</div>")
                    .append("</div></body></html>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(message);
            log.info("Forgot Password OTP Email successfully sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send Forgot Password OTP Email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
