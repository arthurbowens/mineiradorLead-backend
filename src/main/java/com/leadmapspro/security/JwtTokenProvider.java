package com.leadmapspro.security;

import com.leadmapspro.config.LeadMapsProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final LeadMapsProperties properties;

    public JwtTokenProvider(LeadMapsProperties properties) {
        this.properties = properties;
    }

    private SecretKey signingKey() {
        String secret = properties.getJwt().getSecret();
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret deve ter pelo menos 32 bytes (256 bits). Defina leadmaps.jwt.secret ou JWT_SECRET.");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(UUID userId, String email) {
        long minutes = properties.getJwt().getAccessExpirationMinutes();
        Date now = new Date();
        Date exp = new Date(now.getTime() + minutes * 60_000);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        String sub = parseClaims(token).getSubject();
        return UUID.fromString(sub);
    }
}
