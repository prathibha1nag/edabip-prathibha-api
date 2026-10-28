package com.edabip.billing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.edabip.billing.exceptionHandler.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {
    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> disableContainerRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                            ObjectMapper objectMapper) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, exception) -> {
                            response.setStatus(401);
                            response.setHeader("WWW-Authenticate", "Bearer");
                            response.setContentType("application/json");
                            objectMapper.writeValue(response.getOutputStream(),
                                    ApiResponse.failure("UNAUTHORIZED", "A valid Bearer token is required", null));
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
