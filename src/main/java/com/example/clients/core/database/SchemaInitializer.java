package com.example.clients.core.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class SchemaInitializer {

    private static final String SCHEMA_RESOURCE = "/db/clienti_schema.sql";

    private final Database database;
    private boolean initialized;

    public SchemaInitializer(Database database) {
        this.database = database;
    }

    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        database.start();
        try {
            executeSchema(database.getConnection());
            initialized = true;
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Impossibile inizializzare lo schema Clienti.", e);
        }
    }

    private void executeSchema(Connection connection) throws SQLException, IOException {
        for (String statementSql : readStatements()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(statementSql);
            } catch (SQLException e) {
                if (!isAlreadyExistingObject(e)) {
                    throw e;
                }
            }
        }
    }

    private List<String> readStatements() throws IOException {
        InputStream inputStream = SchemaInitializer.class.getResourceAsStream(SCHEMA_RESOURCE);
        if (inputStream == null) {
            throw new IOException("Risorsa schema non trovata: " + SCHEMA_RESOURCE);
        }

        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                current.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    statements.add(current.substring(0, current.lastIndexOf(";")));
                    current.setLength(0);
                }
            }
        }

        if (current.length() > 0) {
            statements.add(current.toString());
        }
        return statements;
    }

    private boolean isAlreadyExistingObject(SQLException e) {
        String sqlState = e.getSQLState();
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return "X0Y32".equals(sqlState)
                || "X0Y68".equals(sqlState)
                || message.contains("already exists")
                || message.contains("già esistente");
    }
}
