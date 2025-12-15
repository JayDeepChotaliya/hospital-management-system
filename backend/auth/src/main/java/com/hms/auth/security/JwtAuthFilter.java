package com.hms.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        logger.debug("JwtAuthFilter - processing request: {} {}", request.getMethod(), request.getRequestURI());
        // 1) Authorization header nikalna
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        // 2) "Bearer <token>" format check
        if(authHeader != null && authHeader.startsWith("Bearer "))
        {
           token = authHeader.substring(7);
           try
           {
               // 3) Token se username nikalna
             username =  jwtUtil.extractUsername(token);
           }
           catch (ExpiredJwtException e)
           {
               logger.info("JWT expired for request to {}: {}", request.getRequestURI(), e.getMessage());
               sendUnauthorized(response, "TOKEN_EXPIRED", "JWT token expired");
               return;
           }
           catch (JwtException e) {
               logger.info("Invalid JWT for request to {}: {}", request.getRequestURI(), e.getMessage());
               sendUnauthorized(response, "INVALID_TOKEN", "JWT token invalid");
               return;
           }
           catch (Exception e)
           {
               logger.error("Error extracting JWT: {}", e.getMessage(), e);
               sendUnauthorized(response, "INVALID_TOKEN", "Invalid JWT token");
               return;
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
                if(token != null && jwtUtil.validateToken(token, userDetails))
                {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                                            null,
                                                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    logger.debug("Authentication set for user {} with authorities {}", userDetails.getUsername(),
                            userDetails.getAuthorities());
                    // 6) SecurityContext me authentication set karo
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                else {
                    logger.info("JWT validation failed for user {}", username);
                    sendUnauthorized(response, "INVALID_TOKEN", "JWT validation failed");
                    return;
                }
            } catch (UsernameNotFoundException ex) {
                logger.info("User from token not found: {}", username);
                sendUnauthorized(response, "USER_NOT_FOUND", "User not found");
                return;
            } catch (Exception ex) {
                logger.error("Could not set user authentication in security context: {}", ex.getMessage(), ex);
                sendUnauthorized(response, "AUTH_ERROR", "Authentication error");
                return;
            }
        }
        // 7) Chain continue karo
        filterChain.doFilter(request, response);

    }

    private void sendUnauthorized(HttpServletResponse response, String errorCode, String message ) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String body = String.format("{\"status\":401,\"error\":\"Unauthorized\",\"errorCode\":\"%s\",\"message\":\"%s\"}",
                errorCode, message);
        response.getWriter().write(body);
    }
}
