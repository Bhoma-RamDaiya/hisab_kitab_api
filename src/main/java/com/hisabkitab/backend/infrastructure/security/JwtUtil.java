package com.hisabkitab.backend.infrastructure.security;

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

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";
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
// 🔹 Generate Token
    public String generateToken(String userName) {
        return Jwts.builder()
                .subject(userName)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)   // NEW
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)  // NEW
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION)
                )
                .signWith(getKey())
                .compact();
    }

    public boolean isAccessToken(String token) {
        try {
            return ACCESS_TOKEN_TYPE.equals(
                    parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class)
            );
        } catch (Exception e) {
            return false;
        }
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
