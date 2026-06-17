package com.example.clients.feature.auth.login.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoginService {

    private final Database database;
    private final SchemaInitializer schemaInitializer;

    public LoginService(Database database) {
        this(database, new SchemaInitializer(database));
    }

    public LoginService(Database database, SchemaInitializer schemaInitializer) {
        this.database = database;
        this.schemaInitializer = schemaInitializer;
    }

    public List<LoginUser> loadUsers() {
        schemaInitializer.initialize();

        String sql = "SELECT ID, NOME, COGNOME, USERNAME FROM OPERATORI WHERE ATTIVO = 1 ORDER BY USERNAME";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<LoginUser> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(new LoginUser(
                        UUID.fromString(resultSet.getString("ID")),
                        valueOrEmpty(resultSet.getString("USERNAME")),
                        valueOrEmpty(resultSet.getString("NOME")),
                        valueOrEmpty(resultSet.getString("COGNOME"))
                ));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento utenti.", e);
        }
    }

    public LoginSession login(LoginUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Seleziona un utente.");
        }

        return new LoginSession(user.id(), user.username(), user.displayName());
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    public record LoginUser(UUID id, String username, String nome, String cognome) {
        public String displayName() {
            String fullName = String.join(" ", nome, cognome).trim();
            return fullName.isBlank() ? username : fullName + " (" + username + ")";
        }

        @Override
        public String toString() {
            return displayName();
        }
    }

    public record LoginSession(UUID userId, String username, String displayName) {
    }
}
