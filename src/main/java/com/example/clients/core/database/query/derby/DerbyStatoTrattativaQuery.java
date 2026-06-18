package com.example.clients.core.database.query.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.core.database.query.StatoTrattativaQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DerbyStatoTrattativaQuery implements StatoTrattativaQuery {

    private final Database database;
    private final SchemaInitializer schemaInitializer;

    public DerbyStatoTrattativaQuery(Database database) {
        this(database, new SchemaInitializer(database));
    }

    public DerbyStatoTrattativaQuery(Database database, SchemaInitializer schemaInitializer) {
        this.database = database;
        this.schemaInitializer = schemaInitializer;
    }

    @Override
    public List<StatoTrattativaRecord> findAll() {
        schemaInitializer.initialize();

        String sql = "SELECT ID, NOME, ORDINE FROM STATI_TRATTATIVA ORDER BY ORDINE, NOME";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<StatoTrattativaRecord> records = new ArrayList<>();
            while (resultSet.next()) {
                records.add(new StatoTrattativaRecord(
                        UUID.fromString(resultSet.getString("ID")),
                        valueOrEmpty(resultSet.getString("NOME")),
                        resultSet.getInt("ORDINE")
                ));
            }
            return records;
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento stati trattativa.", e);
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
