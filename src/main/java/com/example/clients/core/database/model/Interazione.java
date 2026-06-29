package com.example.clients.core.database.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Interazione(
        UUID id,
        UUID clienteId,
        UUID operatoreId,
        String tipo,
        UUID attivitaId,
        LocalDate dataContatto,
        LocalDate prossimoContatto,
        BigDecimal coinvolgimento,
        String testo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
