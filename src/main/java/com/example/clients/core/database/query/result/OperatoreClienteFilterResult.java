package com.example.clients.core.database.query.result;

import java.util.UUID;

public record OperatoreClienteFilterResult(
        UUID id,
        String nome,
        String cognome,
        String username
) {
}
