package com.example.clients.core.database.model;

import java.util.UUID;

public record TipoAttivita(
        UUID id,
        String nome,
        Integer ordine,
        boolean attivo
) {
}
