package com.doctor_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {

        logger.debug("JwtAuthFilter - processing request: {} {}", request.getMethod(), request.getRequestURI());

        // 1) Authorization header nikalna
        final String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer "))
        {
            logger.debug("No Bearer token found in request headers");
            filterChain.doFilter(request,response);
            return;
        }
        String token = authHeader.substring(7).trim();
        try{
            Claims claims = jwtUtil.parseClaims(token);
            String userName = claims.getSubject();

            if(userName == null || userName.isBlank())
            {
                logger.debug("Token parsed but subject (username) missing");
                filterChain.doFilter(request,response);
                return;
            }

            List<String> roles = extractRolesFromClaims(claims);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(String::trim)
                    .filter(r-> !r.isEmpty())
                    .map(String::toUpperCase)
                    .map(r->r.startsWith("ROLE_")? r: "ROLE_"+r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userName, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            logger.debug("Authenticated user={} roles={}", userName, roles);
        }catch (JwtException ex)
        {
            logger.debug("JWT invalid or expired for request {}: {}", request.getRequestURI(), ex.getMessage());
        }
        catch (Exception ex) {
            logger.error("Unexpected error in JwtAuthFilter: {}", ex.getMessage(), ex);
        }
        filterChain.doFilter(request,response);
    }
    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromClaims(Claims claims) {
        Object raw = claims.get("roles");
        if (raw == null) return List.of();

        if (raw instanceof Collection<?>) {
            return ((Collection<?>) raw).stream().map(Object::toString).collect(Collectors.toList());
        }

        // fallback to CSV string
        String s = raw.toString();
        if (s.isBlank()) return List.of();
        return Arrays.stream(s.split(",")).map(String::trim).collect(Collectors.toList());
    }
}
