package com.doctor_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}")
    private String secretRaw;

    private  Key signingKey;

    @PostConstruct
    public void init()
    {
        byte[] keyBytes;

        // Prefer Base64 secret (recommended)
        try{
            keyBytes = Base64.getDecoder().decode(secretRaw);
            log.debug("Using Base64 decoded JWT secret ({} bytes)", keyBytes.length);
        }catch (IllegalArgumentException ex)
        {
            log.debug("JWT secret not Base64, using UTF-8 bytes");
            keyBytes = secretRaw.getBytes(StandardCharsets.UTF_8);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token)
    {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token)
    {
        Object rolesObj = parseClaims(token).get("roles");
        if(rolesObj == null)
            return Set.of();
        if(rolesObj instanceof Collection<?>)
        {
            return ((java.util.Collection<?>) rolesObj)
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.toSet());
        }
        return Set.of(rolesObj.toString().split(","));
    }

    public boolean isExpired(String token)
    {
        Date expiration = parseClaims(token).getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public boolean validateToken(String token, String expectedUsername)
    {
        try{
                Claims claims = parseClaims(token);
                if(expectedUsername != null && !expectedUsername.equals(claims.getSubject()))
                {
                    log.debug("Token username mismatch: expected={}, actual={}",
                    expectedUsername, claims.getSubject());

                    return false;
                }
                if(isExpired(token)){
                    log.debug("Token expired");
                    return false;
                }
                return true;
        }catch (JwtException ex)
        {
            log.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    // Simple "is token valid?" check (no username check)
    public boolean isTokenValid(String token) {
        try {
            return !isExpired(token);
        } catch (JwtException ex) {
            return false;
        }
    }
}
