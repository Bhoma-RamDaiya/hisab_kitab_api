package com.hisabkitab.backend.utils;


import com.hisabkitab.backend.user.domain.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public UserEntity getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof UserEntity)) {

            throw new UsernameNotFoundException("User not authenticated");
        }

        return (UserEntity) authentication.getPrincipal();
    }
}
