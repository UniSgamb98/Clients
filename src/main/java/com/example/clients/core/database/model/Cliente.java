package com.example.clients.core.database.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Cliente(
        UUID id,
        String ragioneSociale,
        String tipoCliente,
        String statoTrattativa,
        String partitaIva,
        String codiceFiscale,
        LocalDate acquisizione,
        UUID operatoreId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
