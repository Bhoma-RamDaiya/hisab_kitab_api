package com.hisabkitab.backend.utils;


import java.util.Random;

public class UsernameGenerator {
    private static final Random random = new Random();
    public static String generateUsername(String name) {

        String cleanName = name
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", "");
        int number = 1000 + random.nextInt(9000);
        return cleanName + number;
    }
}