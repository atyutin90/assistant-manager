package ru.otus.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.otus.config.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties properties;

    @Override
    public String generateToken(UserDetails userDetails) {
        var issuedAt = new Date();
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(issuedAt)
            .setExpiration(new Date(issuedAt.getTime() + properties.getExpirationTimes().toMillis()))
            .signWith(getSiginKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    @Override
    public void writeCookie(HttpServletResponse response, String token) {
        response.addHeader(SET_COOKIE, jwtCookie(token, properties.getExpirationTimes()).toString()
        );
    }

    @Override
    public void clearJwtCookie(HttpServletResponse response) {
        response.addHeader(SET_COOKIE, jwtCookie(StringUtils.EMPTY, Duration.ZERO).toString());
    }

    @Override
    public Optional<String> extractUsername(String token) {
        return ofNullable(extractClaim(token, Claims::getSubject));
    }

    @Override
    public Optional<String> readToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")
            && authorization.length() > "Bearer ".length()) {
            return of(authorization.substring("Bearer ".length()));
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> properties.getCookieName().equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolvers.apply(claims);
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSiginKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSiginKey() {
        byte[] key = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(key);
    }

    private ResponseCookie jwtCookie(String token, Duration maxAge) {
        return ResponseCookie.from(properties.getCookieName(), token)
            .httpOnly(true)
            .secure(properties.isSecureCookie())
            .sameSite("Lax")
            .path("/")
            .maxAge(maxAge)
            .build();
    }
}
