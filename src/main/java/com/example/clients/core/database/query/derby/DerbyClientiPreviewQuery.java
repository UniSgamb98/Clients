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
            "RAGIONE_SOCIALE",
            "TIPO_CLIENTE",
            "STATO_TRATTATIVA"
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
    public ClientePreviewPage findPage(int page, int pageSize, String orderByColumn, boolean ascending) {
        schemaInitializer.initialize();

        int safePage = Math.max(0, page);
        int safePageSize = Math.max(1, pageSize);
        int offset = safePage * safePageSize;
        long totalRows = countAll();
        String sql = "SELECT ID, RAGIONE_SOCIALE, TIPO_CLIENTE, STATO_TRATTATIVA FROM CLIENTI "
                + "ORDER BY " + safeOrderColumn(orderByColumn) + (ascending ? " ASC" : " DESC") + ", ID "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, offset);
            statement.setInt(2, safePageSize);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ClientePreviewRecord> previews = new ArrayList<>();
                while (resultSet.next()) {
                    UUID clienteId = getUuid(resultSet, "ID");
                    previews.add(new ClientePreviewRecord(
                            clienteId,
                            valueOrEmpty(resultSet.getString("RAGIONE_SOCIALE")),
                            valueOrEmpty(resultSet.getString("TIPO_CLIENTE")),
                            firstValue("CONTATTI_CLIENTE", clienteId),
                            firstValue("TELEFONI_CLIENTE", clienteId),
                            firstValue("EMAIL_CLIENTE", clienteId),
                            valueOrEmpty(resultSet.getString("STATO_TRATTATIVA"))
                    ));
                }
                return new ClientePreviewPage(previews, safePage, safePageSize, totalRows);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento anteprima clienti.", e);
        }
    }

    private long countAll() {
        String sql = "SELECT COUNT(*) FROM CLIENTI";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Errore conteggio clienti.", e);
        }
    }

    private String safeOrderColumn(String orderByColumn) {
        if (orderByColumn != null && ALLOWED_ORDER_COLUMNS.contains(orderByColumn)) {
            return orderByColumn;
        }
        return "RAGIONE_SOCIALE";
    }

    private String firstValue(String tableName, UUID clienteId) throws SQLException {
        String sql = "SELECT DESCRIZIONE FROM " + tableName + " WHERE CLIENTE_ID = ? ORDER BY ID FETCH FIRST ROW ONLY";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return valueOrEmpty(resultSet.getString("DESCRIZIONE"));
                }
                return "";
            }
        }
    }

    private UUID getUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
