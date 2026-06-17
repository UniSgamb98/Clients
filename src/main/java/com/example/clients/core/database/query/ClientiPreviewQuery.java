package com.example.clients.core.database.query;

import java.util.List;
import java.util.UUID;

public interface ClientiPreviewQuery {
    ClientePreviewPage findPage(int page, int pageSize, String searchText, String orderByColumn, boolean ascending);

    record ClientePreviewPage(
            List<ClientePreviewRecord> records,
            int page,
            int pageSize,
            long totalRows
    ) {
        public ClientePreviewPage {
            records = List.copyOf(records);
        }
    }

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
