package com.hisabkitab.backend.constant;

public class ApiEndpoints {

    private ApiEndpoints() {
    }

    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/signup",
            "/auth/login",
            "/auth/refresh"
    };

    public static final String[] ADMIN_ENDPOINTS = {
            "/admin/**"
    };

    public static final String[] USER_ENDPOINTS = {
            "/user/**"
    };

    public static final String[] ORGANIZATION_ENDPOINTS = {
            "/organization/**"
    };
}
