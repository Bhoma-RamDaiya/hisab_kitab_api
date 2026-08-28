package com.hisabkitab.backend.user.interfaces;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.infrastructure.security.persistence.RefreshTokenRequest;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.user.application.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@RequestBody SignupRequest request) {
        ApiResponse<AuthResponse> response =
                authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        ApiResponse<AuthResponse> response =
                authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/user")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserByUserId() {
        ApiResponse<UserProfileResponse> response = authService.getUserByUserId();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return authService.getAllUsers();
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>>
    refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                authService.refreshToken(request)
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>>
    logout(
            @RequestBody LogoutRequest request
    ) {

        return ResponseEntity.ok(
                authService.logout(request.getRefreshToken()));

    }
}
