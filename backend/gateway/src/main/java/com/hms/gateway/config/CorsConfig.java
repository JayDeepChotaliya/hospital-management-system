package com.hms.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cc = new CorsConfiguration();
        // Use explicit origins for security (dev + prod)
        cc.setAllowedOrigins(Arrays.asList("http://localhost:4200", "https://your-production-domain.com"));
        // Or, if using Spring Boot 2.4+ and need wildcard patterns, use setAllowedOriginPatterns
        // cc.setAllowedOriginPatterns(Arrays.asList("http://localhost:4200", "https://*.your-production-domain.com"));

        cc.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cc.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        cc.setAllowCredentials(true);
        cc.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cc);
        return new CorsWebFilter(source);
    }
}