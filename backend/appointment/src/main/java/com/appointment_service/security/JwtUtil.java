package com.appointment_service.security;

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

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret:}")
    private String secretRaw;
    private Key key;

    @PostConstruct
    public void init()
    {
        if(secretRaw == null || secretRaw.isBlank())
        {
            log.warn("JWT secret is empty; generating a random key for runtime only (NOT for prod).");
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            return;
        }
        byte[]  keyBytes;
        try{
            keyBytes = Base64.getDecoder().decode(secretRaw);
            log.debug("Using Base64-decoded JWT secret ({} bytes)", keyBytes.length);
        }
        catch (IllegalArgumentException e) {
            // fallback to UTF-8 bytes
            log.debug("JWT secret is not Base64; using UTF-8 bytes");
            keyBytes = secretRaw.getBytes(StandardCharsets.UTF_8);
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.debug("Jwt signing key initialized");
    }


    public Jws<Claims> parseToken(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public String extractUsername(String token) {
        return parseToken(token)
                .getBody()
                .getSubject();
    }

    public String extractRolesString(String token) {
        Object r = parseToken(token).getBody().get("roles");
        return r == null ? "" : r.toString();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

}
