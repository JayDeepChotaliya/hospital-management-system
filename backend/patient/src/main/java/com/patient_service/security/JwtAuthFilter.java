package com.patient_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class JwtAuthFilter extends OncePerRequestFilter
{
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private  JwtUtil jwtUtil;

//    public JwtAuthFilter(JwtUtil jwtUtil)
//    {this.jwtUtil = jwtUtil;}

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {

        logger.debug("Inside JwtAuthFilter#doFilterInternal");

        // 1) Authorization header nikalna
        final String authHeader = request.getHeader("Authorization");

        System.out.println("Auth Header = " + authHeader);

        // 2) "Bearer <token>" format check

        if(authHeader != null && authHeader.startsWith("Bearer "))
        {
            String token = authHeader.substring(7);

            if(jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoleString(token);

                List<SimpleGrantedAuthority> auths = roles.isBlank() ? List.of() :
                        Arrays.stream(roles.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username,null,auths);
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Authenticated user={} roles={}", username, roles);
            }
            else {
                logger.debug("Invalid JWT");
            }
        }
        filterChain.doFilter(request, response);
    }
}
