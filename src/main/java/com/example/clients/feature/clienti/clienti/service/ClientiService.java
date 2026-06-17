package com.example.clients.feature.clienti.clienti.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.query.ClientiPreviewQuery;
import com.example.clients.core.database.query.derby.DerbyClientiPreviewQuery;

import java.util.List;
import java.util.UUID;

public class ClientiService {

    private final ClientiPreviewQuery clientiPreviewQuery;

    public ClientiService(Database database) {
        this(new DerbyClientiPreviewQuery(database));
    }

    public ClientiService(ClientiPreviewQuery clientiPreviewQuery) {
        this.clientiPreviewQuery = clientiPreviewQuery;
    }

    public ClientiPage getClientiPreview(ClientiSearchRequest request) {
        ClientiPreviewQuery.ClientePreviewPage page = clientiPreviewQuery.findPage(
                request.page(),
                request.pageSize(),
                request.searchText(),
                request.sortColumn().sqlColumn(),
                request.ascending()
        );
        return new ClientiPage(
                page.records().stream()
                        .map(this::toPreviewRow)
                        .toList(),
                page.page(),
                page.pageSize(),
                page.totalRows()
        );
    }

    private ClientePreviewRow toPreviewRow(ClientiPreviewQuery.ClientePreviewRecord record) {
        return new ClientePreviewRow(
                record.clienteId(),
                new ClientePreview(
                        record.ragioneSociale(),
                        record.tipoCliente(),
                        record.referente(),
                        record.telefono(),
                        record.email(),
                        record.statoTrattativa()
                )
        );
    }

    public enum SortColumn {
        NAME("C.RAGIONE_SOCIALE"),
        TYPE("C.TIPO_CLIENTE"),
        CONTACT("REFERENTE"),
        PHONE("TELEFONO"),
        EMAIL("EMAIL"),
        STATUS("C.STATO_TRATTATIVA");

        private final String sqlColumn;

        SortColumn(String sqlColumn) {
            this.sqlColumn = sqlColumn;
        }

        private String sqlColumn() {
            return sqlColumn;
        }
    }

    public record ClientiSearchRequest(
            int page,
            int pageSize,
            String searchText,
            SortColumn sortColumn,
            boolean ascending
    ) {
        public ClientiSearchRequest {
            page = Math.max(0, page);
            pageSize = Math.max(1, pageSize);
            searchText = searchText == null ? "" : searchText.trim();
            sortColumn = sortColumn == null ? SortColumn.NAME : sortColumn;
        }
    }

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

    public record ClientePreviewRow(
            UUID clienteId,
            ClientePreview preview
    ) {
    }

    public record ClientePreview(
            String name,
            String type,
            String contact,
            String phone,
            String email,
            String status
    ) {
    }
}
