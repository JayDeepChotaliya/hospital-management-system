package com.hms.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil
{
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private Key key;

    // Secret key application.properties se lete hain

    public JwtUtil(@Value("${app.jwt.secret}") String secret)
    {
        System.out.println(this.getClass()+" ***** Encoding Key **** ");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        System.out.println(this.getClass()+"    Generated Key is  = "+this.key);
    }

    public String generateToken(String username)
    {
        System.out.println(this.getClass()+" ***** Generating  Token  **** ");

        long expiration = 1000 * 60 * 60;

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public boolean validateToken(String token, String username)
    {
        String extractedUsername = extractUsername(token);
        boolean isTokenValidate = extractedUsername.equals(username) && !isTokenExpired(token);

        System.out.println(this.getClass() + "isTokenValidate  == " +isTokenValidate );
        return isTokenValidate;

    }

    public boolean isTokenExpired(String token)
    {
        boolean isTokenExpired = extractAllClaims(token).getExpiration().before(new Date());
        System.out.println(this.getClass() + "isTokenExpired  == " +isTokenExpired );
        return isTokenExpired;
    }

    public String extractUsername(String token)
    {
        System.out.println(this.getClass() + " **** Extract Username ***");
        String extractedUsername = extractAllClaims(token).getSubject();
        System.out.println(this.getClass() + "Extracted Username = "+ extractedUsername);
        return extractedUsername;
    }

    public Claims extractAllClaims(String token)
    {
        System.out.println(this.getClass()+" ***In extractAllClaims ***");
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        System.out.println(this.getClass()+"  Claims = "+claims);
        return claims;
    }

    public String extractRolesString(String token) {
        Object r = parseToken(token).getBody().get("roles");
        return r == null ? "" : r.toString();
    }

}
