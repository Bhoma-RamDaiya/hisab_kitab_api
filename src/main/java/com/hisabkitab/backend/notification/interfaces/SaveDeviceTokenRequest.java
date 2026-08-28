package com.hisabkitab.backend.notification.interfaces;

import com.hisabkitab.backend.notification.interfaces.DeviceType;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveDeviceTokenRequest {

//    @NotBlank(message = "Device id is required.")
    private String deviceId;

//    @NotBlank(message = "Device name is required.")
    private String deviceName;

//    @NotNull(message = "Device type is required.")
    private DeviceType deviceType;

//    @NotBlank(message = "FCM token is required.")
    private String fcmToken;
}
