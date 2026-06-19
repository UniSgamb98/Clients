package com.example.clients.feature.clienti.clienti.dto;

import java.util.List;

public record ClientiPage(
        List<ClientePreviewRow> rows,
        int page,
        int pageSize,
        long totalRows
) {
    public ClientiPage {
        rows = List.copyOf(rows);
    }

    public int totalPages() {
        if (totalRows == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRows / pageSize);
    }

    public boolean hasPreviousPage() {
        return page > 0;
    }

    public boolean hasNextPage() {
        return page + 1 < totalPages();
    }
}
