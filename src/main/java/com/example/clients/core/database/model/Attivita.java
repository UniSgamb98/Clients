package com.example.clients.core.database.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Attivita(
        UUID id,
        String titolo,
        String descrizione,
        Integer priorita,
        String stato,
        LocalDate dataInizio,
        LocalDate dataFine,
        UUID tipoAttivitaId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
