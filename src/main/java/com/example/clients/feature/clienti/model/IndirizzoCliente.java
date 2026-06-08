package com.example.clients.feature.clienti.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record IndirizzoCliente(
        UUID id,
        UUID clienteId,
        String paese,
        String regione,
        String provincia,
        String citta,
        String indirizzo,
        String numeroCivico,
        String cap,
        boolean principale,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
