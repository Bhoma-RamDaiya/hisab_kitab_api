package com.hisabkitab.backend.user.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.user.domain.UserRole;
import com.hisabkitab.backend.infrastructure.security.JwtUtil;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.interfaces.LoginRequest;
import com.hisabkitab.backend.infrastructure.security.persistence.RefreshTokenRequest;
import com.hisabkitab.backend.user.interfaces.SignupRequest;
import com.hisabkitab.backend.user.interfaces.AuthResponse;
import com.hisabkitab.backend.user.interfaces.UserProfileResponse;
import com.hisabkitab.backend.infrastructure.security.persistence.RefreshTokenEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.infrastructure.security.persistence.RefreshTokenRepository;
import com.hisabkitab.backend.user.infrastructure.UserRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import com.hisabkitab.backend.utils.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    private static final int MAX_FAILED_ATTEMPTS = 30;
    private static final long LOCKOUT_MINUTES = 1;
 @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    public ApiResponse<AuthResponse> signup(
            SignupRequest request
    ) {

        if (request.getEmail() != null &&
                userRepository.findByEmail(
                        request.getEmail()
                ).isPresent()) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }

        String generatedUsername =
                generateUniqueUsername(
                        request.getName()
                );

        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .username(generatedUsername).gender(request.getGender())
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        String token =
                jwtUtil.generateToken(
                        user.getUsername()
                );
        String refreshToken =
                jwtUtil.generateRefreshToken(
                        user.getUsername()
                );
        AuthResponse authResponse =
                AuthResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .accessToken(token)
                        .userRole(user.getRole())
                        .name(user.getName())
                        .refreshToken(refreshToken)
                        .build();

        RefreshTokenEntity refreshTokenEntity =
                RefreshTokenEntity.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .revoked(false)
                        .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return ApiResponse
                .<AuthResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(authResponse)
                .build();
    }
    public ApiResponse<AuthResponse> refreshToken(
            RefreshTokenRequest request
    ) {
        if (jwtUtil.isAccessToken(request.getRefreshToken())) {
            throw new RuntimeException(
                    "Invalid refresh token"
            );
        }
        RefreshTokenEntity refreshTokenEntity =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid refresh token"));
        if (refreshTokenEntity.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token revoked"
            );
        }

        if (refreshTokenEntity.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Refresh token expired"
            );
        }

        UserEntity user =
                refreshTokenEntity.getUser();

        String newAccessToken =
                jwtUtil.generateToken(
                        user.getUsername()
                );

        AuthResponse authResponse =
                AuthResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .accessToken(newAccessToken)
                        .refreshToken(request.getRefreshToken())
                        .userRole(user.getRole())
                        .name(user.getName())
                        .build();

        return ApiResponse
                .<AuthResponse>builder()
                .success(true)
                .message("Token refreshed")
                .data(authResponse)
                .build();
    }


    public ApiResponse<AuthResponse> login(LoginRequest request) {

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Account temporarily locked due to multiple failed attempts. "
                            + "Please try again later."
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(
                        LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES)
                );
                user.setFailedLoginAttempts(0);
            }

            userRepository.save(user);

            throw new RuntimeException("Invalid email or password");
        }

        /*
         * Successful login - reset the counter.
         */
        if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }


        String token =
                jwtUtil.generateToken(
                        user.getUsername()
                );
        String refreshToken =
                jwtUtil.generateRefreshToken(
                        user.getUsername()
                );
        AuthResponse authResponse =
                AuthResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .accessToken(token)
                        .userRole(user.getRole())
                        .name(user.getName())
                        .refreshToken(refreshToken)
                        .build();

        RefreshTokenEntity refreshTokenEntity =
                RefreshTokenEntity.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .revoked(false)
                        .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return ApiResponse
                .<AuthResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(authResponse)
                .build();

//        return jwtUtil.generateToken(user.getUsername());
    }
    public ApiResponse<UserProfileResponse> getUserByUserId() {
        UserEntity user = securityUtils.getCurrentUser();
        if(user != null){
            int count =
                    organizationMemberRepository
                            .countByUserIdAndRoleAndStatus(
                                    user.getId(),
                                    OrganizationRole.OWNER,
                                    MembershipStatus.ACTIVE
                            );
            boolean hasOrganization = count>=1;

         UserProfileResponse response =   UserProfileResponse.builder().userRole(user.getRole())
                    .id(user.getId())
                    .name(user.getName())
                    .username(user.getUsername())
                    .phoneNumber(user.getPhoneNumber())
                    .email(user.getEmail())
                    .gender(user.getGender())
                 .hasOrganization(hasOrganization)
                 .organizationCount(count)
                    .build();
            return  ApiResponse
                    .<UserProfileResponse>builder()
                    .success(true)
                    .message("User Fetched successfully")
                    .data(response)
                    .build();
        } else {
            return  ApiResponse.<UserProfileResponse>builder()
                    .success(false)
                    .message("User Fetched successfully")
                    .data(null)
                    .build();
        }
//        return user.orElse(null);
    }
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();

    }

    public ApiResponse<String> logout(String refreshToken) {

        RefreshTokenEntity tokenEntity =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() -> new RuntimeException("Token not found"));
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);
       refreshTokenRepository.deleteById(tokenEntity.getId());
        return ApiResponse.<String>builder()
                .success(false)
                .message("User Fetched successfully")
                .data("Logged out successfully")
                .build();

    }
    private String generateUniqueUsername(String name) {

        String username;

        do {

            username =
                    UsernameGenerator.generateUsername(name);

        } while (userRepository.existsByUsername(username));

        return username;
    }
}
