package com.appointment_service.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter
{
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
       final String authHeader = request.getHeader("Authorization");

       if(authHeader == null || !authHeader.startsWith("Bearer "))
       {
           logger.debug("No Bearer token found in request headers");
           filterChain.doFilter(request,response);
           return;
       }

       final String token = authHeader.substring(7).trim();
       try{
           if(!jwtUtil.isValid(token))
           {
               logger.debug("JWT validation failed");
               filterChain.doFilter(request, response);
               return;
           }
           String username = jwtUtil.extractUsername(token);
           if (username == null || username.isBlank()){
               logger.debug("JWT contains no subject");
               filterChain.doFilter(request, response);
               return;
           }
           String rolesRaw = jwtUtil.extractRolesString(token);
           List<String> roles = parseRoles(rolesRaw);

           List<SimpleGrantedAuthority> authorities = roles.stream()
                   .map(String::trim)
                   .filter(r -> !r.isEmpty())
                   .map(String::toUpperCase)
                   .map(r -> r.startsWith("ROLE_") ? r : "ROLES_"+r)
                   .map(SimpleGrantedAuthority::new)
                   .collect(Collectors.toList());

           UsernamePasswordAuthenticationToken auth =
                   new UsernamePasswordAuthenticationToken(username, null, authorities);
           SecurityContextHolder.getContext().setAuthentication(auth);
           logger.debug("Authenticated user={} roles={}", username, roles);
       }catch (JwtException ex) {
           logger.debug("JWT parsing/validation failed: {}", ex.getMessage());
           // do not set authentication — allow security layer to return 401
       } catch (Exception ex) {
           logger.error("Unexpected error in JwtAuthFilter: {}", ex.getMessage(), ex);
       }
       filterChain.doFilter(request,response);
    }

    private List<String> parseRoles(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        // attempt: JSON-array-like string: ["ROLE_A","ROLE_B"] or plain CSV "A,B"
        String s = raw.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            // remove brackets and quotes, split by comma
            String inner = s.substring(1, s.length() - 1);
            return Arrays.stream(inner.split(","))
                    .map(r -> r.replaceAll("^\\s*\"|\"\\s*$", "").trim()) // remove surrounding quotes
                    .filter(r -> !r.isEmpty())
                    .collect(Collectors.toList());
        }

        // fallback CSV
        if (s.contains(",")) {
            return Arrays
                    .stream(s.split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .collect(Collectors.toList());
        }
        // single role
        return List.of(s);
    }
}
