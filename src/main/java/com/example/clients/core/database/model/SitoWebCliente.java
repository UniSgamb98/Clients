package com.example.clients.core.database.model;

import java.util.UUID;

public record SitoWebCliente(
        UUID id,
        UUID clienteId,
        String descrizione
) {
}
