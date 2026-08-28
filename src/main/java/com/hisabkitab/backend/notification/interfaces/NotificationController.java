package com.hisabkitab.backend.notification.interfaces;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.notification.application.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @PostMapping("/device-token")
    public ApiResponse<String> saveDeviceToken(
             @RequestBody SaveDeviceTokenRequest request) {

        return notificationService.saveDeviceToken(request);
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications() {

        return notificationService.getNotifications();
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<String> markAsRead(
            @PathVariable Long notificationId) {

        return notificationService.markAsRead(notificationId);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadNotificationCount() {

        return notificationService.getUnreadNotificationCount();
    }
}
