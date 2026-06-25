package com.example.clients.core.database.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttivitaCliente(
        UUID id,
        UUID attivitaId,
        UUID clienteId,
        String stato,
        UUID interazioneId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
