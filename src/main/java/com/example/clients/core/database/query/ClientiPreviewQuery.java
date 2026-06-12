package com.example.clients.core.database.query;

import java.util.List;
import java.util.UUID;

public interface ClientiPreviewQuery {
    List<ClientePreviewRecord> findAll();

    record ClientePreviewRecord(
            UUID clienteId,
            String ragioneSociale,
            String tipoCliente,
            String referente,
            String telefono,
            String email,
            String statoTrattativa
    ) {
    }
}
