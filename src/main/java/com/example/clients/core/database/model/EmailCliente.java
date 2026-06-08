package com.example.clients.core.database.model;

import java.util.UUID;

public record EmailCliente(
        UUID id,
        UUID clienteId,
        UUID contattoId,
        String descrizione
) {
}
