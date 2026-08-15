package com.hisabkitab.backend.config.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

//    private static final String SECRET = "mysecretkeymysecretkeymysecretkey"; // 32+ chars
@Value("${jwt.secret}")
private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    private static final long ACCESS_TOKEN_EXPIRATION =
            1000 * 60*15 ;
    private static final long REFRESH_TOKEN_EXPIRATION =
            1000L * 60 * 60 * 24 * 30;
//    private SecretKey getKey() {
//        return Keys.hmacShaKeyFor(SECRET.getBytes());
//    }

    // 🔹 Generate Token
    public String generateToken(String userName) {
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)) // 1 hour
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(String username) {

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + REFRESH_TOKEN_EXPIRATION)
                )
                .signWith(getKey())
                .compact();
    }

    // 🔹 Extract Email
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 🔹 Validate Token
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔹 Internal parsing (NEW STYLE)
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
