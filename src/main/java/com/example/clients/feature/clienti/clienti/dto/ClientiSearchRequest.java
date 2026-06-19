package com.example.clients.feature.clienti.clienti.dto;

import java.util.UUID;

public record ClientiSearchRequest(
        int page,
        int pageSize,
        String searchText,
        UUID operatoreId,
        String tipoCliente,
        String statoTrattativa,
        SortColumn sortColumn,
        boolean ascending
) {
    public ClientiSearchRequest {
        page = Math.max(0, page);
        pageSize = Math.max(1, pageSize);
        searchText = searchText == null ? "" : searchText.trim();
        tipoCliente = tipoCliente == null ? "" : tipoCliente.trim();
        statoTrattativa = statoTrattativa == null ? "" : statoTrattativa.trim();
        sortColumn = sortColumn == null ? SortColumn.NAME : sortColumn;
    }
}
