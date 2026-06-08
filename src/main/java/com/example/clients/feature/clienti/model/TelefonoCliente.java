package com.example.clients.feature.clienti.model;

import java.util.UUID;

public record TelefonoCliente(
        UUID id,
        UUID clienteId,
        String descrizione
) {
}
