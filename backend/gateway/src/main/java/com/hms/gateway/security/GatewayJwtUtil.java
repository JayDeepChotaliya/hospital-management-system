package com.hms.gateway.security;

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
public class GatewayJwtUtil {

    private static final Logger log = LoggerFactory.getLogger(GatewayJwtUtil.class);

    @Value("${app.jwt.secret:}")
    private String secretRaw;

    private Key key;

    @PostConstruct
    public void inti()
    {
        if(secretRaw == null || secretRaw.isEmpty())
        {
            log.warn("JWT secret not set — generating runtime key (dev only)");
            this.secretRaw = String.valueOf(Keys.secretKeyFor(SignatureAlgorithm.HS256));
            return;
        }

        byte[] keyBytes;
        try{
            keyBytes = Base64.getDecoder().decode(secretRaw);
            log.debug("Using Base64-decoded jwt secret");
        }catch (IllegalArgumentException ex)
        {
            keyBytes = secretRaw.getBytes(StandardCharsets.UTF_8);
            log.debug("Using raw-UTF8 jwt secret");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.debug("GatewayJwtUtil initialized");
    }

    public Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public Object extractClaim(String token, String claim) {
        return parseToken(token).getBody().get(claim);
    }

}
