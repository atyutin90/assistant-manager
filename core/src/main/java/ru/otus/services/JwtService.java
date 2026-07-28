package ru.otus.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface JwtService {

    String generateToken(UserDetails userDetails);

    void writeCookie(HttpServletResponse response, String token);

    void clearJwtCookie(HttpServletResponse response);

    Optional<String> extractUsername(String token);

    Optional<String> readToken(HttpServletRequest request);
}
