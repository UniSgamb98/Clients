package com.example.clients.feature.clienti.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AttivitaCliente(
        UUID id,
        UUID clienteId,
        UUID contattoId,
        UUID operatoreId,
        String tipo,
        LocalDate dataAttivita,
        String esito,
        LocalDate prossimaAttivita,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
