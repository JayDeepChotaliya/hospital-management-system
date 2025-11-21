package com.doctor_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Jws<Claims> parseToken(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public String extractUsername(String token)
    {
        return parseToken(token).getBody().getSubject();
    }

    public String extractRoleString(String token)
    {
        Object r = parseToken(token).getBody().get("roles");
        return r == null ? "" : r.toString();
    }

    public boolean isTokenValid(String token)
    {
        try
        {
           parseToken(token);
           return true;
        } catch (JwtException e)
        {
                return false;
        }
    }

}
