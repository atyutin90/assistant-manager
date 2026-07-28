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

import java.util.Set;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static ru.otus.entity.enums.UserRole.TEAM_LEAD;
import static ru.otus.entity.enums.UserRole.USER;
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

    private void configureAuthorizeHttpRequests(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
            getPermitAll(auth)
                .requestMatchers("/verifications/**").hasRole(TEAM_LEAD.name())
                .anyRequest().hasAnyRole(USER.name(), TEAM_LEAD.name()));
    }

    @Bean
    public AccessPolicy userLoginAccessPolicy() {
        var allowedAuthorities = Set.of(ROLE + USER.name(), ROLE + TEAM_LEAD.name());
        return authentication -> authentication.getAuthorities().stream()
            .anyMatch(authority -> allowedAuthorities.contains(authority.getAuthority()));
    }
}
