package com.placement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.dto.Result;
import com.placement.util.ApiMessages;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String API_PATTERN = "/api/**";

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    org.springframework.security.web.csrf.CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookieCustomizer(cookie -> cookie.httpOnly(true).sameSite("Strict"));
        return repository;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http.csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository())
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(context -> context.securityContextRepository(securityContextRepository))
            .authorizeHttpRequests(auth -> auth
                // Frontend assets remain public; aptitude workflows require an authenticated role.
                .requestMatchers("/", "/index.html", "/login.html", "/css/**", "/js/**", "/webapp/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/aptitude-tests/*/submit").hasRole(ROLE_STUDENT)
                .requestMatchers(HttpMethod.GET, "/api/aptitude-tests/*/review").hasRole(ROLE_STUDENT)
                .requestMatchers(HttpMethod.GET, "/api/aptitude-tests/*/questions/manage").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/aptitude-tests").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/aptitude-tests/*").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/aptitude-tests/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/aptitude-tests/*/questions").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/aptitude-tests/*/questions/*").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/aptitude-tests/*/questions/*").hasRole(ROLE_ADMIN)
                .requestMatchers("/api/aptitude-scores/student/**").hasRole(ROLE_STUDENT)
                .requestMatchers("/api/aptitude-scores/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/attendances/bulk").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/attendances/student/**").hasRole(ROLE_STUDENT)
                .requestMatchers("/api/attendances/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/trainings/*/video-completion").hasRole(ROLE_STUDENT)
                .requestMatchers("/api/aptitude-tests/**").hasAnyRole(ROLE_ADMIN, ROLE_STUDENT)
                .requestMatchers(HttpMethod.POST, API_PATTERN).hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, API_PATTERN).hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, API_PATTERN).hasRole(ROLE_ADMIN)
                .requestMatchers(API_PATTERN).permitAll()
                .anyRequest().permitAll())
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, authException) -> {
                    Result<String> error = Result.error(401, ApiMessages.get("api.error.authenticationRequired"));
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write(objectMapper.writeValueAsString(error));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    Result<String> error = Result.error(403, ApiMessages.get("api.error.PTSE003.message"));
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write(objectMapper.writeValueAsString(error));
                }));
        return http.build();
    }
}
