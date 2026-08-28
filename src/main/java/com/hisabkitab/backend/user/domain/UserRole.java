package com.hisabkitab.backend.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER(1, "User"),
//    ORGANIZATION(2, "Organization"),
//    WORKER(3, "Worker"),
    SUPER_ADMIN(4, "Admin");
    private final int value;
    private final String text;
}
