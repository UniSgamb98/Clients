package com.example.clients.feature.auth.login.service;

public class LoginService {

    public LoginSession login(String username, String password) {
        String cleanUsername = clean(username);

        if (cleanUsername.isBlank()) {
            throw new IllegalArgumentException("Inserisci il nome utente.");
        }

        return new LoginSession(cleanUsername);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record LoginSession(String username) {
    }
}
