package com.example.clients.feature.clienti.clienti.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.query.ClientiPreviewQuery;
import com.example.clients.core.database.query.derby.DerbyClientiPreviewQuery;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ClientiService {

    private final ClientiPreviewQuery clientiPreviewQuery;
    private SortColumn lastSortColumn;
    private boolean ascending = true;

    public ClientiService(Database database) {
        this(new DerbyClientiPreviewQuery(database));
    }

    public ClientiService(ClientiPreviewQuery clientiPreviewQuery) {
        this.clientiPreviewQuery = clientiPreviewQuery;
    }

    public List<ClientePreviewRow> getClientiPreview() {
        return clientiPreviewQuery.findAll().stream()
                .map(this::toPreviewRow)
                .toList();
    }

    public List<ClientePreviewRow> sortClientiBy(SortColumn sortColumn) {
        if (sortColumn == lastSortColumn) {
            ascending = !ascending;
        } else {
            lastSortColumn = sortColumn;
            ascending = true;
        }

        Comparator<ClientePreviewRow> comparator = Comparator.comparing(
                sortColumn.getValueExtractor(),
                String.CASE_INSENSITIVE_ORDER
        );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return getClientiPreview().stream()
                .sorted(comparator)
                .toList();
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
        NAME(row -> row.preview().name()),
        TYPE(row -> row.preview().type()),
        CONTACT(row -> row.preview().contact()),
        PHONE(row -> row.preview().phone()),
        EMAIL(row -> row.preview().email()),
        STATUS(row -> row.preview().status());

        private final Function<ClientePreviewRow, String> valueExtractor;

        SortColumn(Function<ClientePreviewRow, String> valueExtractor) {
            this.valueExtractor = valueExtractor;
        }

        private Function<ClientePreviewRow, String> getValueExtractor() {
            return valueExtractor;
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
