package com.example.clients.core.database.query.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.core.database.query.ClientiPreviewQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DerbyClientiPreviewQuery implements ClientiPreviewQuery {

    private static final Set<String> ALLOWED_ORDER_COLUMNS = Set.of(
            "C.RAGIONE_SOCIALE",
            "C.TIPO_CLIENTE",
            "REFERENTE",
            "TELEFONO",
            "EMAIL",
            "C.STATO_TRATTATIVA"
    );

    private final Database database;
    private final SchemaInitializer schemaInitializer;

    public DerbyClientiPreviewQuery(Database database) {
        this(database, new SchemaInitializer(database));
    }

    public DerbyClientiPreviewQuery(Database database, SchemaInitializer schemaInitializer) {
        this.database = database;
        this.schemaInitializer = schemaInitializer;
    }

    @Override
    public ClientePreviewPage findPage(int page, int pageSize, String searchText, String orderByColumn, boolean ascending) {
        schemaInitializer.initialize();

        int safePage = Math.max(0, page);
        int safePageSize = Math.max(1, pageSize);
        int offset = safePage * safePageSize;
        String cleanSearchText = cleanSearchText(searchText);
        boolean hasSearch = !cleanSearchText.isBlank();
        long totalRows = countAll(cleanSearchText);
        String sql = "SELECT C.ID, C.RAGIONE_SOCIALE, C.TIPO_CLIENTE, C.STATO_TRATTATIVA, "
                + "COALESCE(R.REFERENTE, '') AS REFERENTE, "
                + "COALESCE(T.TELEFONO, '') AS TELEFONO, "
                + "COALESCE(E.EMAIL, '') AS EMAIL "
                + "FROM CLIENTI C "
                + previewAggregateJoin("CONTATTI_CLIENTE", "R", "REFERENTE")
                + previewAggregateJoin("TELEFONI_CLIENTE", "T", "TELEFONO")
                + previewAggregateJoin("EMAIL_CLIENTE", "E", "EMAIL")
                + searchWhereClause(hasSearch)
                + "ORDER BY " + safeOrderColumn(orderByColumn) + (ascending ? " ASC" : " DESC") + ", C.ID "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            int parameterIndex = bindSearchParameters(statement, cleanSearchText, hasSearch, 1);
            statement.setInt(parameterIndex, offset);
            statement.setInt(parameterIndex + 1, safePageSize);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ClientePreviewRecord> previews = new ArrayList<>();
                while (resultSet.next()) {
                    previews.add(new ClientePreviewRecord(
                            getUuid(resultSet, "ID"),
                            valueOrEmpty(resultSet.getString("RAGIONE_SOCIALE")),
                            valueOrEmpty(resultSet.getString("TIPO_CLIENTE")),
                            valueOrEmpty(resultSet.getString("REFERENTE")),
                            valueOrEmpty(resultSet.getString("TELEFONO")),
                            valueOrEmpty(resultSet.getString("EMAIL")),
                            valueOrEmpty(resultSet.getString("STATO_TRATTATIVA"))
                    ));
                }
                return new ClientePreviewPage(previews, safePage, safePageSize, totalRows);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento anteprima clienti.", e);
        }
    }

    private String previewAggregateJoin(String tableName, String alias, String valueAlias) {
        return "LEFT JOIN ("
                + "SELECT CLIENTE_ID, MIN(DESCRIZIONE) AS " + valueAlias + " "
                + "FROM " + tableName + " "
                + "GROUP BY CLIENTE_ID"
                + ") " + alias + " ON " + alias + ".CLIENTE_ID = C.ID ";
    }

    private long countAll(String searchText) {
        boolean hasSearch = !searchText.isBlank();
        String sql = "SELECT COUNT(*) FROM CLIENTI C " + searchWhereClause(hasSearch);
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            bindSearchParameters(statement, searchText, hasSearch, 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore conteggio clienti.", e);
        }
    }

    private String searchWhereClause(boolean hasSearch) {
        if (!hasSearch) {
            return "";
        }

        return "WHERE LOWER(C.RAGIONE_SOCIALE) LIKE ? "
                + "OR LOWER(C.TIPO_CLIENTE) LIKE ? "
                + "OR LOWER(C.STATO_TRATTATIVA) LIKE ? "
                + "OR EXISTS (SELECT 1 FROM CONTATTI_CLIENTE CC WHERE CC.CLIENTE_ID = C.ID AND LOWER(CC.DESCRIZIONE) LIKE ?) "
                + "OR EXISTS (SELECT 1 FROM TELEFONI_CLIENTE T WHERE T.CLIENTE_ID = C.ID AND LOWER(T.DESCRIZIONE) LIKE ?) "
                + "OR EXISTS (SELECT 1 FROM EMAIL_CLIENTE E WHERE E.CLIENTE_ID = C.ID AND LOWER(E.DESCRIZIONE) LIKE ?) ";
    }

    private int bindSearchParameters(PreparedStatement statement, String searchText, boolean hasSearch, int startIndex) throws SQLException {
        if (!hasSearch) {
            return startIndex;
        }

        String pattern = "%" + searchText + "%";
        int parameterIndex = startIndex;
        for (int i = 0; i < 6; i++) {
            statement.setString(parameterIndex++, pattern);
        }
        return parameterIndex;
    }

    private String cleanSearchText(String searchText) {
        return searchText == null ? "" : searchText.trim().toLowerCase();
    }

    private String safeOrderColumn(String orderByColumn) {
        if (orderByColumn != null && ALLOWED_ORDER_COLUMNS.contains(orderByColumn)) {
            return orderByColumn;
        }
        return "C.RAGIONE_SOCIALE";
    }

    private UUID getUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
