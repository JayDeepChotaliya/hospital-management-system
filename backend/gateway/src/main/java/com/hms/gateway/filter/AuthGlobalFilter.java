package com.hms.gateway.filter;

import com.hms.gateway.security.GatewayJwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import io.jsonwebtoken.JwtException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private final GatewayJwtUtil jwtUtil;

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        String path = uri.getPath();
        log.info("Gateway request path={}", path);

        // Public endpoints (allow)
        if (path.startsWith("/api/v1/auth") || path.startsWith("/actuator") || path.startsWith("/api-docs") || path.startsWith("/swagger")) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = exchange.getRequest().getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if (authHeaders.isEmpty()) {
            return unauthorized(exchange, "Missing Authorization header");
        }

        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();

        try {
            if (!jwtUtil.validateToken(token)) {
                return unauthorized(exchange, "Invalid or expired token");
            }

            String username = jwtUtil.extractUsername(token);
            Object rolesObj = jwtUtil.extractClaim(token, "roles");
            String roles = rolesObj == null ? "" : rolesObj.toString();

            var mutated = exchange.getRequest().mutate()
                    .header("X-User", username != null ? username : "")
                    .header("X-Roles", roles)
                    .build();

            ServerWebExchange newExchange = exchange.mutate().request(mutated).build();
            return chain.filter(newExchange);

        } catch (JwtException ex) {
            log.warn("JWT validation error: {}", ex.getMessage());
            return unauthorized(exchange, "JWT validation error");
        } catch (Exception ex) {
            log.error("Unexpected error validating token: {}", ex.getMessage(), ex);
            return unauthorized(exchange, "Token validation error");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                msg, exchange.getRequest().getURI().getPath());
        byte[] bytes = body.getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(bytes)));
    }


}
