package com.hisabkitab.backend.notification.application;

import com.hisabkitab.backend.notification.domain.NotificationType;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.notification.interfaces.SaveDeviceTokenRequest;
import com.hisabkitab.backend.notification.interfaces.NotificationResponse;
import com.hisabkitab.backend.notification.domain.NotificationEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.notification.domain.UserDeviceTokenEntity;
import com.hisabkitab.backend.infrastructure.firebase.FcmService;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.notification.infrastructure.NotificationRepository;
import com.hisabkitab.backend.notification.infrastructure.UserDeviceTokenRepository;

import com.hisabkitab.backend.utils.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationServiceImpl {

    private final NotificationRepository notificationRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final SecurityUtils securityUtils;
    private final FcmService fcmService;
    @Override
    @Transactional
    public ApiResponse<String> saveDeviceToken(
            SaveDeviceTokenRequest request) {

        UserEntity currentUser = securityUtils.getCurrentUser();

        UserDeviceTokenEntity deviceToken =
                userDeviceTokenRepository
                        .findByUserIdAndDeviceId(
                                currentUser.getId(),
                                request.getDeviceId()
                        )
                        .orElse(null);

        if (deviceToken == null) {

            deviceToken = UserDeviceTokenEntity.builder()
                    .user(currentUser)
                    .deviceId(request.getDeviceId())
                    .deviceName(request.getDeviceName())
                    .deviceType(request.getDeviceType())
                    .fcmToken(request.getFcmToken())
                    .isActive(true)
                    .lastUsedAt(LocalDateTime.now())
                    .build();

        } else {

            deviceToken.setDeviceName(request.getDeviceName());
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setFcmToken(request.getFcmToken());
            deviceToken.setIsActive(true);
            deviceToken.setLastUsedAt(LocalDateTime.now());
        }

        userDeviceTokenRepository.save(deviceToken);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Device token saved successfully.")
                .data("Device token saved successfully.")
                .build();
    }

    @Override
    public ApiResponse<List<NotificationResponse>> getNotifications() {

        UserEntity currentUser = securityUtils.getCurrentUser();

        List<NotificationResponse> notifications =
                notificationRepository
                        .findAllByUserIdOrderByCreatedAtDesc(currentUser.getId())
                        .stream()
                        .map(notification ->
                                NotificationResponse.builder()
                                        .id(notification.getId())
                                        .title(notification.getTitle())
                                        .message(notification.getMessage())
                                        .notificationType(
                                                notification.getNotificationType())
                                        .referenceId(
                                                notification.getReferenceId())
                                        .isRead(notification.getIsRead())
                                        .readAt(notification.getReadAt())
                                        .createdAt(notification.getCreatedAt())
                                        .build()
                        )
                        .toList();

        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched successfully.")
                .data(notifications)
                .build();
    }


    @Override
    @Transactional
    public ApiResponse<String> markAsRead(Long notificationId) {

        UserEntity currentUser = securityUtils.getCurrentUser();

        NotificationEntity notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found."));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "You are not authorized to update this notification.");
        }

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return ApiResponse.<String>builder()
                    .success(true)
                    .message("Notification is already marked as read.")
                    .data("Notification is already marked as read.")
                    .build();
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        notificationRepository.save(notification);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Notification marked as read successfully.")
                .data("Notification marked as read successfully.")
                .build();
    }



    @Override
    public ApiResponse<Long> getUnreadNotificationCount() {

        UserEntity currentUser = securityUtils.getCurrentUser();

        long count = notificationRepository
                .countByUserIdAndIsReadFalse(currentUser.getId());

        return ApiResponse.<Long>builder()
                .success(true)
                .message("Unread notification count fetched successfully.")
                .data(count)
                .build();
    }
    @Override
    @Transactional
    public ApiResponse<String> sendNotification(
            UserEntity user,
            OrganizationEntity organization,
            NotificationType notificationType,
            String title,
            String message,
            Long referenceId) {

        NotificationEntity notification =
                NotificationEntity.builder()
                        .user(user)
                        .organization(organization)
                        .title(title)
                        .message(message)
                        .notificationType(notificationType)
                        .referenceId(referenceId)
                        .isRead(false)
                        .build();

        notificationRepository.save(notification);

        List<UserDeviceTokenEntity> devices =
                userDeviceTokenRepository
                        .findAllByUserIdAndIsActiveTrue(user.getId());

        for (UserDeviceTokenEntity device : devices) {

            try {

                fcmService.sendNotification(
                        device.getFcmToken(),
                        title,
                        message,
                        notification.getId()
                );

            } catch (Exception e) {

                // Don't fail the whole notification process
                // because one device token is invalid.

                device.setIsActive(false);

                userDeviceTokenRepository.save(device);
            }
        }

        return ApiResponse.<String>builder()
                .success(true)
                .message("Notification sent successfully.")
                .data("Notification sent successfully.")
                .build();
    }

}
