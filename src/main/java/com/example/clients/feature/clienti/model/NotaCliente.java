package com.example.clients.feature.clienti.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotaCliente(
        UUID id,
        UUID clienteId,
        UUID operatoreId,
        String testo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
