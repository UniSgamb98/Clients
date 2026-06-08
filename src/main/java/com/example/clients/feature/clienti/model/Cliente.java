package com.example.clients.feature.clienti.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Cliente(
        UUID id,
        String ragioneSociale,
        String tipoCliente,
        String interessamento,
        String partitaIva,
        String codiceFiscale,
        double coinvolgimento,
        int checkpoint,
        LocalDate acquisizione,
        UUID operatoreId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
