package com.hisabkitab.backend.user.interfaces;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
