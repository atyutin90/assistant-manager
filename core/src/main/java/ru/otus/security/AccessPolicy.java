package ru.otus.security;

import org.springframework.security.core.Authentication;

public interface AccessPolicy {
    boolean canLogin(Authentication authentication);
}
