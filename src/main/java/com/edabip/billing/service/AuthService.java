package com.edabip.billing.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.edabip.billing.model.TokenResponse;
import com.edabip.billing.model.AuthUser;
import com.edabip.billing.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository users;
    private final JwtService jwtService;

    public AuthService(UserRepository users, JwtService jwtService) {
        this.users = users;
        this.jwtService = jwtService;
    }

    public TokenResponse login(String email, String suppliedPassword) {
        AuthUser user = users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(candidate -> "ACTIVE".equalsIgnoreCase(candidate.status()))
                .filter(candidate -> candidate.password().equals(suppliedPassword))
                .orElseThrow(InvalidCredentialsException::new);
        users.updateLastLogin(user.userId());
        return new TokenResponse(jwtService.createToken(user.email(), user.role()), "Bearer",
                jwtService.getExpirationSeconds());
    }

    public static class InvalidCredentialsException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
