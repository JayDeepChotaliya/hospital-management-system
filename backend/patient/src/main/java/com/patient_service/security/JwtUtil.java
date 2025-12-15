package com.patient_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil
{

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final String secretRaw;
    private  Key signingKey;

    public JwtUtil(@Value("${app.jwt.secret:}") String secretRaw)
    {
        this.secretRaw = secretRaw;
    }

    @PostConstruct
    private void init() {
        if (secretRaw == null || secretRaw.isBlank()) {
            log.info("JWT secret is not configured (app.jwt.secret). Application cannot validate tokens.");
            throw new IllegalStateException("Missing app.jwt.secret - configure same secret as auth-service");
        }

        byte[] keyBytes;
        try {
                keyBytes = Base64.getDecoder().decode(secretRaw);
                log.debug("Using base64-decoder JWT secret (length={})",keyBytes.length);
        }catch (IllegalArgumentException ex)
        {
                log.debug("JWT secret not base64, using UTF-8 bytes");
                keyBytes = secretRaw.getBytes(StandardCharsets.UTF_8);
        }
        if(keyBytes.length < 32)
        {
            log.warn("JWT secret is weak ({} bytes). Provide at least 32 bytes (256 bits) for HS256.", keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaim(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token)
    {
        return parseClaim(token).getSubject();
    }

    public Set<String> extractRole(String token)
    {
        Object raw = parseClaim(token).get("roles");
        if(raw == null)
            return Set.of();
        if(raw instanceof java.util.Collection<?>)
        {
            return ((java.util.Collection<?>) raw)
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.toSet());

        }
        return Set.of(raw.toString());
    }

    public Long extractUserId(String token)
    {
        Object uid = parseClaim(token).get("userId");
        if (uid == null)
            return null;
        try{
            return  Long.valueOf(uid.toString());

        }catch (NumberFormatException ex)
        {
                return null;
        }
    }

    public boolean isTokenExpired(String token)
    {
        Date exp = parseClaim(token).getExpiration();
        return exp != null && exp.before(new Date());
    }

    public boolean validateToken(String token , String expectedUsername)
    {
        try{
                Claims claims = parseClaim(token);
                if(expectedUsername != null && !expectedUsername.equals(claims.getSubject()))
                {
                    log.debug("Token subject mismatch: token={}, expected={}", claims.getSubject(), expectedUsername);
                    return false;
                }
                return !isTokenExpired(token);

        }catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

}
