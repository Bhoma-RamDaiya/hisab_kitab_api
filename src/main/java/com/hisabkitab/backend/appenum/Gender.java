package com.hisabkitab.backend.appenum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE(1, "Male"),
    FEMALE(2, "Female"),
    OTHER(3, "Other");

    private final int value;
    private final String text;
}