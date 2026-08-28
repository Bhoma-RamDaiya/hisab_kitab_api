package com.hisabkitab.backend.notification.application;

import com.hisabkitab.backend.notification.domain.NotificationType;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.notification.interfaces.SaveDeviceTokenRequest;
import com.hisabkitab.backend.notification.interfaces.NotificationResponse;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.user.domain.UserEntity;

import java.util.List;

public interface NotificationServiceImpl {

    ApiResponse<String> saveDeviceToken(
            SaveDeviceTokenRequest request
    );

    ApiResponse<List<NotificationResponse>> getNotifications();

    ApiResponse<String> markAsRead(Long notificationId);


    ApiResponse<Long> getUnreadNotificationCount();

    ApiResponse<String> sendNotification(
            UserEntity user,
            OrganizationEntity organization,
            NotificationType notificationType,
            String title,
            String message,
            Long referenceId
    );
}
