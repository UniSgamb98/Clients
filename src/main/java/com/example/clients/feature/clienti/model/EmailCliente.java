package com.example.clients.feature.clienti.model;

import java.util.UUID;

public record EmailCliente(
        UUID id,
        UUID clienteId,
        String descrizione
) {
}
