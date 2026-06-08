package com.example.clients.feature.clienti.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmailCliente(
        UUID id,
        UUID clienteId,
        UUID contattoId,
        String email,
        String tipo,
        boolean principale,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
