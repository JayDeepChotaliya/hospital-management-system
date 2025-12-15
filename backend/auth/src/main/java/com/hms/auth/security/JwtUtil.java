package com.hms.auth.security;

import com.hms.auth.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil
{
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final String secretRaw;
    private final long expirationMs;
    private Key signingKey;

    public JwtUtil(@Value("${app.jwt.secret}" ) String secretRaw,
                   @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.secretRaw = secretRaw;
        this.expirationMs = expirationMs;
    }

    // Secret key application.properties se lete hain
    @PostConstruct
    private void init()
    {
        // If secret is base64-encoded, decode it. Otherwise use raw bytes.
        byte[] keyBytes = secretRaw.getBytes();
        try{
            keyBytes = java.util.Base64.getDecoder().decode(secretRaw);
        }
        catch (IllegalArgumentException e)
        {
            // not base64 — fall back to UTF-8 bytes
            keyBytes = secretRaw.getBytes(StandardCharsets.UTF_8);
        }
        // Ensure key length is sufficient for HS256 (>= 32 bytes)
        if(keyBytes.length < 32)
        {
            logger.warn("JWT secret is weak ({} bytes). Provide a 256-bit+ secret (base64 recommended).", keyBytes.length);

        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(String username , Set<String> roles)
    {
        long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(signingKey,SignatureAlgorithm.HS256);

        if (roles != null && !roles.isEmpty()) {
            builder.claim("roles", roles.stream().collect(Collectors.toSet()));
        }
        return builder.compact();
    }

    public boolean validateToken(String token, UserDetails userDetails)
    {
        try
        {
            Claims claims = parseClaims(token);
            String subject = claims.getSubject();
            boolean notExpired = !claims.getExpiration().before(new Date());
            return subject != null && subject.equals(userDetails.getUsername()) && notExpired;

        }
        catch (JwtException | IllegalArgumentException e) {
            logger.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }

    }

    public String extractUsername(String token)
    {
        return parseClaims(token).getSubject();
    }
    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
    }

    public Claims parseClaims(String token) {
        // this will throw appropriate JwtException (ExpiredJwtException, MalformedJwtException, etc.)
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Set<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles == null) return java.util.Collections.emptySet();
        if (roles instanceof java.util.Collection<?>) {
            return ((java.util.Collection<?>) roles).stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of(roles.toString());
    }

    public long getExpirationMs() {
        return expirationMs;
    }

}
