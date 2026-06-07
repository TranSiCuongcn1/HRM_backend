package com.hrm.backend.service.notification.channel.impl;

import com.hrm.backend.service.notification.channel.NotificationChannel;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    public EmailNotificationChannel(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String recipient, String title, String content) {
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("Cannot send email: recipient address is empty.");
            return;
        }

        try {
            log.info("Sending Email to {}...", recipient);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient);
            helper.setSubject(title);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email successfully sent to {}", recipient);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipient, e.getMessage(), e);
        }
    }
}
