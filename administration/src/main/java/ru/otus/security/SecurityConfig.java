package ru.otus.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ViewResolver;
import ru.otus.services.JwtService;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static ru.otus.entity.enums.UserRole.ADMIN;
import static ru.otus.security.BaseSecurityConfiguration.ROLE;
import static ru.otus.security.BaseSecurityConfiguration.configureExceptionHandling;
import static ru.otus.security.BaseSecurityConfiguration.getPermitAll;
import static ru.otus.security.BaseSecurityConfiguration.loginForm;
import static ru.otus.security.BaseSecurityConfiguration.logout;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtFilter,
        AuthenticationSuccessHandler jwtAuthenticationSuccessHandler,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
        @Qualifier("thymeleafViewResolver") ViewResolver viewResolver
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(loginForm(jwtAuthenticationSuccessHandler))
            .logout(logout(jwtService))
            .exceptionHandling(ex -> configureExceptionHandling(ex, exceptionResolver, viewResolver))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));
        configureAuthorizeHttpRequests(http);
        return http.build();
    }

    @Bean
    public AccessPolicy adminLoginAccessPolicy() {
        String adminAuthority = ROLE + ADMIN.name();
        return authentication -> authentication.getAuthorities().stream()
            .anyMatch(authority -> adminAuthority.equals(authority.getAuthority()));
    }

    private void configureAuthorizeHttpRequests(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
            getPermitAll(auth)
                .anyRequest().hasRole(ADMIN.name()));
    }
}
