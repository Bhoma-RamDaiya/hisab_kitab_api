package com.hisabkitab.backend.notification.interfaces;

import com.hisabkitab.backend.notification.domain.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private NotificationType notificationType;

    private Long referenceId;

    private Boolean isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}
