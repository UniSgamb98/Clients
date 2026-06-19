package com.example.clients.feature.clienti.clienti.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.query.ClientiFilterQuery;
import com.example.clients.core.database.query.ClientiPreviewQuery;
import com.example.clients.core.database.query.derby.DerbyClientiFilterQuery;
import com.example.clients.core.database.query.derby.DerbyClientiPreviewQuery;
import com.example.clients.core.database.query.result.OperatoreClienteFilterResult;

import java.util.List;
import java.util.UUID;

public class ClientiService {

    private final ClientiPreviewQuery clientiPreviewQuery;
    private final ClientiFilterQuery clientiFilterQuery;

    public ClientiService(Database database) {
        this(new DerbyClientiPreviewQuery(database), new DerbyClientiFilterQuery(database));
    }

    public ClientiService(ClientiPreviewQuery clientiPreviewQuery, ClientiFilterQuery clientiFilterQuery) {
        this.clientiPreviewQuery = clientiPreviewQuery;
        this.clientiFilterQuery = clientiFilterQuery;
    }

    public ClientiPage getClientiPreview(ClientiSearchRequest request) {
        ClientiPreviewQuery.ClientePreviewPage page = clientiPreviewQuery.findPage(
                request.page(),
                request.pageSize(),
                request.searchText(),
                request.operatoreId(),
                request.tipoCliente(),
                request.statoTrattativa(),
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

    public List<OperatoreFilter> getOperatorFilters() {
        return clientiFilterQuery.findOperatoriConClienti().stream()
                .map(this::toOperatoreFilter)
                .toList();
    }

    public List<TextFilter> getTipoClienteFilters() {
        return clientiFilterQuery.findTipiCliente().stream()
                .map(value -> new TextFilter(value, value))
                .toList();
    }

    public List<TextFilter> getStatoTrattativaFilters() {
        return clientiFilterQuery.findStatiTrattativa().stream()
                .map(value -> new TextFilter(value, value))
                .toList();
    }

    private OperatoreFilter toOperatoreFilter(OperatoreClienteFilterResult result) {
        return new OperatoreFilter(
                result.id(),
                operatorLabel(result.nome(), result.cognome(), result.username())
        );
    }

    private String operatorLabel(String nome, String cognome, String username) {
        String fullName = String.join(" ", valueOrEmpty(nome), valueOrEmpty(cognome)).trim();
        String cleanUsername = valueOrEmpty(username);
        if (fullName.isBlank()) {
            return cleanUsername;
        }
        return cleanUsername.isBlank() ? fullName : fullName + " (" + cleanUsername + ")";
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
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

    public record OperatoreFilter(UUID id, String label) {
        public static OperatoreFilter empty() {
            return new OperatoreFilter(null, "Tutti");
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public record TextFilter(String value, String label) {
        public static TextFilter empty(String label) {
            return new TextFilter("", label);
        }

        public boolean isEmptyOption() {
            return value == null || value.isBlank();
        }

        @Override
        public String toString() {
            return label;
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
