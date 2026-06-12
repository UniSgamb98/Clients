package com.example.clients.core.database.service;

import java.util.UUID;

public final class CurrentOperatoreService {

    public static final UUID DEFAULT_OPERATORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String DEFAULT_USERNAME = "utente";

    public UUID currentOperatoreId() {
        return DEFAULT_OPERATORE_ID;
    }
}
