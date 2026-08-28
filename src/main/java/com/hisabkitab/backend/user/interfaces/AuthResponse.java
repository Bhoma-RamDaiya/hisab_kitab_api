package com.hisabkitab.backend.user.interfaces;

import com.hisabkitab.backend.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private Long userId;

    private String username;

    private String accessToken;

    private String refreshToken;

    private UserRole userRole;

    private String name;
}