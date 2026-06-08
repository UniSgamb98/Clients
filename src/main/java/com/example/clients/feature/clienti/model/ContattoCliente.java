package com.example.clients.feature.clienti.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContattoCliente(
        UUID id,
        UUID clienteId,
        String nome,
        String cognome,
        String ruolo,
        boolean principale,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public String nomeCompleto() {
        String safeNome = nome == null ? "" : nome.trim();
        String safeCognome = cognome == null ? "" : cognome.trim();
        return (safeCognome + " " + safeNome).trim();
    }
}
