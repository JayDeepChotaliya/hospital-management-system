package com.appointment_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("$(app.jwt.secret)") String secret)
    {
        if(secret == null || secret.length() < 32) {
            System.out.println("⚠️ Provided secret too weak or missing. Generating secure random key...");
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // auto-generate strong key
        } else {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        }

        //this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Jws<Claims> parseToken(String token)
    {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String extractUsername(String token) {
        return parseToken(token).getBody().getSubject();
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
            return false;
        }
    }

}
