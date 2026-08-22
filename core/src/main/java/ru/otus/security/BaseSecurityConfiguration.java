package ru.otus.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import ru.otus.services.JwtService;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Configuration
public class BaseSecurityConfiguration {

    public static final String ROLE = "ROLE_";

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
        JwtAuthenticationFilter filter
    ) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationSuccessHandler jwtAuthenticationSuccessHandler(
        JwtService jwtService,
        List<AccessPolicy> loginAccessPolicies
    ) {
        return (request, response, authentication) -> {
            boolean canLogin = loginAccessPolicies.stream().allMatch(policy -> policy.canLogin(authentication));
            if (!canLogin) {
                jwtService.clearJwtCookie(response);
                response.sendRedirect(request.getContextPath() + "/login?forbidden");
                return;
            }
            var userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            jwtService.writeCookie(response, token);
            response.sendRedirect(request.getContextPath() + "/");
        };
    }

    public static AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry getPermitAll(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        return auth
            .requestMatchers(
                "/login",
                "/images/**",
                "/css/**",
                "/actuator/**"
            ).permitAll();
    }

    public static Customizer<FormLoginConfigurer<HttpSecurity>> loginForm(
        AuthenticationSuccessHandler jwtAuthenticationSuccessHandler
    ) {
        return form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler(jwtAuthenticationSuccessHandler)
            .failureUrl("/login?error")
            .permitAll();
    }

    public static Customizer<LogoutConfigurer<HttpSecurity>> logout(JwtService jwtService) {
        return logout -> logout
            .logoutUrl("/logout")
            .addLogoutHandler((request, response, authentication) ->
                jwtService.clearJwtCookie(response))
            .logoutSuccessUrl("/login?logout")
            .permitAll();
    }

    public static void configureExceptionHandling(
        ExceptionHandlingConfigurer<HttpSecurity> exceptions,
        HandlerExceptionResolver exceptionResolver,
        ViewResolver viewResolver
    ) {
        exceptions.accessDeniedHandler((req, res, ex) ->
            handleAccessDenied(req, res, ex, exceptionResolver, viewResolver));
    }

    private static void handleAccessDenied(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Exception exception,
                                           HandlerExceptionResolver exceptionResolver,
                                           ViewResolver viewResolver
    ) throws IOException, ServletException {
        var modelAndView = exceptionResolver.resolveException(request, response, null, exception);
        if (modelAndView == null) {
            response.sendError(FORBIDDEN.value());
            return;
        }
        try {
            render(modelAndView, request, response, viewResolver);
        } catch (Exception ex) {
            throw new ServletException("Failed to render page", ex);
        }
    }

    private static void render(ModelAndView modelAndView,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               ViewResolver viewResolver
    ) throws Exception {
        if (modelAndView.getStatus() != null) {
            response.setStatus(modelAndView.getStatus().value());
        }

        var view = modelAndView.getView();
        if (view == null && modelAndView.getViewName() != null) {
            view = viewResolver.resolveViewName(modelAndView.getViewName(), request.getLocale());
        }
        if (view == null) {
            response.sendError(FORBIDDEN.value());
            return;
        }
        view.render(modelAndView.getModel(), request, response);
    }
}
