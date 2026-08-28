package com.hisabkitab.backend.infrastructure.firebase;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    @Override
    public void sendNotification(
            String fcmToken,
            String title,
            String message,
            Long notificationId) {

        try {

            Message firebaseMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(message)
                                    .build()
                    )
                    .putData(
                            "notificationId",
                            String.valueOf(notificationId)
                    )
                    .build();

            FirebaseMessaging.getInstance()
                    .send(firebaseMessage);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to send push notification.",
                    e
            );
        }
    }
}
