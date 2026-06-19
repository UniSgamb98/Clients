package com.example.clients.core.database.query.record;

import java.util.UUID;

public record OperatoreClienteFilterRecord(
        UUID id,
        String nome,
        String cognome,
        String username
) {
}
