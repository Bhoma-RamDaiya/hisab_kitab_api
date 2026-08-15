package com.hisabkitab.backend.dto.response;

import com.hisabkitab.backend.appenum.UserRole;
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