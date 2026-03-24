package com.leadmapspro.config;

import com.leadmapspro.security.JwtAuthenticationFilter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationEntryPoint json401EntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Credenciais ausentes ou token inválido.\"}");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(json401EntryPoint()))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh")
                                        .permitAll()
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .denyAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(Environment env) {
        CorsConfiguration cfg = new CorsConfiguration();
        /*
         * Render define CORS_ORIGINS; Spring também aceita LEADMAPS_CORS_ALLOWED_ORIGINS.
         * Lê direto do Environment para não depender só do binding em LeadMapsProperties.
         */
        String raw =
                firstNonBlank(
                        env.getProperty("CORS_ORIGINS"),
                        env.getProperty("LEADMAPS_CORS_ALLOWED_ORIGINS"),
                        env.getProperty("leadmaps.cors.allowed-origins"),
                        "http://localhost:4200");
        List<String> patterns = new ArrayList<>();
        for (String o : raw.split(",")) {
            String t = o.trim();
            if (!t.isEmpty()) {
                patterns.add(t);
            }
        }
        cfg.setAllowedOriginPatterns(patterns);
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.setExposedHeaders(java.util.List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return "";
        }
        for (String s : candidates) {
            if (s != null && !s.isBlank()) {
                return s.trim();
            }
        }
        return "";
    }
}
