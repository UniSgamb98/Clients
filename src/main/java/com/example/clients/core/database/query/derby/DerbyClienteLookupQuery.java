package com.example.clients.core.database.query.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.core.database.query.ClienteLookupQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DerbyClienteLookupQuery implements ClienteLookupQuery {

    private final Database database;
    private final SchemaInitializer schemaInitializer;

    public DerbyClienteLookupQuery(Database database) {
        this(database, new SchemaInitializer(database));
    }

    public DerbyClienteLookupQuery(Database database, SchemaInitializer schemaInitializer) {
        this.database = database;
        this.schemaInitializer = schemaInitializer;
    }

    @Override
    public LookupValues findValues() {
        schemaInitializer.initialize();
        return new LookupValues(
                distinctValues("CLIENTI", "TIPO_CLIENTE"),
                distinctValues("CLIENTI", "STATO_TRATTATIVA"),
                distinctValues("TELEFONI_CLIENTE", "DESCRIZIONE"),
                distinctValues("EMAIL_CLIENTE", "DESCRIZIONE")
        );
    }

    private List<String> distinctValues(String tableName, String columnName) {
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName
                + " WHERE " + columnName + " IS NOT NULL AND TRIM(" + columnName + ") <> '' ORDER BY " + columnName;
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> values = new ArrayList<>();
            while (resultSet.next()) {
                values.add(resultSet.getString(columnName));
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento suggerimenti cliente.", e);
        }
    }
}
