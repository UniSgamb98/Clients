package com.example.clients.feature.clienti.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContattoEseguitoCliente(
        UUID id,
        UUID clienteId,
        UUID contattoId,
        UUID operatoreId,
        UUID notaId,
        String tipo,
        LocalDate dataContatto,
        String esito,
        LocalDate prossimoContatto,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
