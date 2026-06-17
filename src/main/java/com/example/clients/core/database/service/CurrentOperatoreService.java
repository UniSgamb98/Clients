package com.example.clients.core.database.service;

import java.util.UUID;

public final class CurrentOperatoreService {

    public static final UUID DEFAULT_OPERATORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String DEFAULT_USERNAME = "utente";

    private static UUID currentOperatoreId = DEFAULT_OPERATORE_ID;
    private static String currentUsername = DEFAULT_USERNAME;

    public static void setCurrentOperatore(UUID operatoreId, String username) {
        currentOperatoreId = operatoreId == null ? DEFAULT_OPERATORE_ID : operatoreId;
        currentUsername = username == null || username.isBlank() ? DEFAULT_USERNAME : username.trim();
    }

    public UUID currentOperatoreId() {
        return currentOperatoreId;
    }

    public String currentUsername() {
        return currentUsername;
    }
}
