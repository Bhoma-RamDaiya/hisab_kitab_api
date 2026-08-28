package com.hisabkitab.backend.infrastructure.security;
import com.hisabkitab.backend.infrastructure.security.JwtUtil;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.user.infrastructure.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            /*
             * Only a genuine access token may authenticate a request.
             * A refresh token must never work as a bearer credential.
             */
            if (jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                String username = jwtUtil.extractUsername(token);

                UserEntity user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities());

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
