package com.hisabkitab.backend.infrastructure.firebase;

public interface FcmService {
    void sendNotification(
            String fcmToken,
            String title,
            String message,
            Long notificationId
    );
}
