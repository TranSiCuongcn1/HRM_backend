package com.hrm.backend.service.notification.sender.impl;

import com.hrm.backend.service.notification.channel.NotificationChannel;
import com.hrm.backend.service.notification.channel.impl.EmailNotificationChannel;
import com.hrm.backend.service.notification.sender.NotificationSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender extends NotificationSender {

    private final EmailNotificationChannel emailNotificationChannel;

    public EmailNotificationSender(EmailNotificationChannel emailNotificationChannel) {
        this.emailNotificationChannel = emailNotificationChannel;
    }

    @Override
    protected NotificationChannel createNotificationChannel() {
        return emailNotificationChannel;
    }
}
