package com.example.clients.feature.clienti.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record SitoWebCliente(
        UUID id,
        UUID clienteId,
        String url,
        String tipo,
        boolean principale,
        String descrizione,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
