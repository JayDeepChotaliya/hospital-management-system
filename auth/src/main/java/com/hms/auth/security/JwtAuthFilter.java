package com.hms.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        logger.debug("Inside JwtAuthFilter#doFilterInternal");

        // 1) Authorization header nikalna
        final String authHeader = request.getHeader("Authorization");

        System.out.println("Auth Header = " + authHeader);

        String token = null;
        String username = null;

        // 2) "Bearer <token>" format check

        if(authHeader != null && authHeader.startsWith("Bearer "))
        {
           token = authHeader.substring(7);
            System.out.println(" Token = "+ token);
           try
           {
               // 3) Token se username nikalna
             username =  jwtUtil.extractUsername(token);
           }
           catch (ExpiredJwtException e)
           {
               logger.warn("JWT token expired: {}", e.getMessage());
           }
           catch (JwtException e) {
               logger.warn("JWT token invalid: {}", e.getMessage());
           }
           catch (Exception e)
           {
               logger.warn("Error parsing JWT: {}", e.getMessage());
           }

        }
        else
        {
            logger.debug("No Bearer token found in request headers");
        }

        // 4) Agar username mil gaya aur SecurityContext me koi authentication nahi hai, to authenticate karo

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            try{
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // 5) Token valid hai ya nahi check karo
                if(token != null && jwtUtil.validateToken(token, userDetails.getUsername()))
                {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                                            null,
                                                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    logger.info("User authenticated with roles: {}", userDetails.getAuthorities());

                    // 6) SecurityContext me authentication set karo
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }



            } catch (UsernameNotFoundException ex) {
                logger.warn("User not found for username from token: {}", username);
            } catch (Exception ex) {
                logger.warn("Could not set user authentication in security context: {}", ex.getMessage());
            }
        }
        // 7) Chain continue karo
        filterChain.doFilter(request, response);
    }
}
