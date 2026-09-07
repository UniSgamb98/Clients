package com.example.clients.core.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

final class TestSchemaLoader {

    private static final String SCHEMA_RESOURCE = "/db/clienti_schema.sql";

    private TestSchemaLoader() {
    }

    static void load(Connection connection) throws SQLException {
        try (InputStream input = TestSchemaLoader.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Risorsa schema non trovata: " + SCHEMA_RESOURCE);
            }
            executeStatements(connection, input);
        } catch (IOException e) {
            throw new IllegalStateException("Lettura dello schema di test non riuscita.", e);
        }
    }

    private static void executeStatements(Connection connection, InputStream input) throws IOException, SQLException {
        StringBuilder currentStatement = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                currentStatement.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    execute(connection, currentStatement.substring(0, currentStatement.lastIndexOf(";")));
                    currentStatement.setLength(0);
                }
            }
        }

        if (!currentStatement.isEmpty()) {
            execute(connection, currentStatement.toString());
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
