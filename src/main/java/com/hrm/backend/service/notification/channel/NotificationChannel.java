package com.hrm.backend.service.notification.channel;

public interface NotificationChannel {
    void send(String recipient, String title, String content);
}
