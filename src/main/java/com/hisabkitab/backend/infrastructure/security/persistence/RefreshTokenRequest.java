package com.hisabkitab.backend.infrastructure.security.persistence;

import lombok.Data;

@Data
public class RefreshTokenRequest {

        private String refreshToken;
}