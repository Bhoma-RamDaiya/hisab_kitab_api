package com.hisabkitab.backend.utils;


import com.hisabkitab.backend.entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public UserEntity getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof UserEntity)) {

            throw new RuntimeException("User is not authenticated");
        }

        return (UserEntity) authentication.getPrincipal();
    }
}
