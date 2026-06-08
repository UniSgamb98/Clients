package com.example.clients.feature.clienti.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record TelefonoCliente(
        UUID id,
        UUID clienteId,
        UUID contattoId,
        String numero,
        String tipo,
        boolean principale,
        String descrizione,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
