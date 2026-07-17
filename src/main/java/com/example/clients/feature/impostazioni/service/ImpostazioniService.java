package com.example.clients.feature.impostazioni.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.feature.impostazioni.dto.Forno;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;

public class ImpostazioniService {
    private static final Set<String> TABLES = Set.of("MATERIALI_DI_CONSUMO", "CANALI_DI_ACQUISTO", "FRESATORI", "FORNI", "CERAMICA", "FRESE");
    private final Database database;
    private final SchemaInitializer schemaInitializer;

    public ImpostazioniService(Database database) {
        this.database = database;
        this.schemaInitializer = new SchemaInitializer(database);
    }

    public List<Forno> getForni() {
        schemaInitializer.initialize();
        try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT ID, TECNOLOGIA, MARCA, MODELLO FROM FORNI ORDER BY TECNOLOGIA, MARCA, MODELLO");
             ResultSet resultSet = statement.executeQuery()) {
            List<Forno> forni = new ArrayList<>();
            while (resultSet.next()) {
                forni.add(new Forno(UUID.fromString(resultSet.getString("ID")), resultSet.getString("TECNOLOGIA"), resultSet.getString("MARCA"), resultSet.getString("MODELLO")));
            }
            return forni;
        } catch (Exception e) {
            throw new RuntimeException("Caricamento forni non riuscito.", e);
        }
    }

    public void saveForni(List<Forno> forni) {
        schemaInitializer.initialize();
        try (PreparedStatement delete = database.getConnection().prepareStatement("DELETE FROM FORNI");
             PreparedStatement insert = database.getConnection().prepareStatement("INSERT INTO FORNI (ID, TECNOLOGIA, MARCA, MODELLO) VALUES (?, ?, ?, ?)")) {
            delete.executeUpdate();
            for (Forno forno : forni) {
                if (forno.tecnologia().isBlank() && forno.marca().isBlank() && forno.modello().isBlank()) continue;
                insert.setString(1, (forno.id() == null ? UUID.randomUUID() : forno.id()).toString());
                insert.setString(2, forno.tecnologia());
                insert.setString(3, forno.marca());
                insert.setString(4, forno.modello());
                insert.addBatch();
            }
            insert.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException("Salvataggio forni non riuscito.", e);
        }
    }

    public List<com.example.clients.feature.impostazioni.dto.ImpostazioneVoce> getVoci(String table, List<String> columns) {
        validate(table, columns); schemaInitializer.initialize();
        try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT ID, " + String.join(", ", columns) + " FROM " + table + " ORDER BY " + columns.get(0)); ResultSet rs = statement.executeQuery()) {
            List<com.example.clients.feature.impostazioni.dto.ImpostazioneVoce> result = new ArrayList<>();
            while (rs.next()) { List<String> values = new ArrayList<>(); for (String column : columns) values.add(rs.getString(column)); result.add(new com.example.clients.feature.impostazioni.dto.ImpostazioneVoce(UUID.fromString(rs.getString("ID")), values)); }
            return result;
        } catch (Exception e) { throw new RuntimeException("Caricamento impostazioni non riuscito.", e); }
    }

    public void saveVoci(String table, List<String> columns, List<com.example.clients.feature.impostazioni.dto.ImpostazioneVoce> voci) {
        validate(table, columns); schemaInitializer.initialize();
        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.size() + 1, "?"));
        try (PreparedStatement delete = database.getConnection().prepareStatement("DELETE FROM " + table); PreparedStatement insert = database.getConnection().prepareStatement("INSERT INTO " + table + " (ID, " + String.join(", ", columns) + ") VALUES (" + placeholders + ")")) {
            delete.executeUpdate();
            for (var voce : voci) { if (voce.valori().stream().allMatch(value -> value == null || value.isBlank())) continue; insert.setString(1, (voce.id() == null ? UUID.randomUUID() : voce.id()).toString()); for (int i=0;i<columns.size();i++) insert.setString(i+2, voce.valori().get(i)); insert.addBatch(); }
            insert.executeBatch();
        } catch (Exception e) { throw new RuntimeException("Salvataggio impostazioni non riuscito.", e); }
    }

    private void validate(String table, List<String> columns) { if (!TABLES.contains(table) || columns.isEmpty() || columns.stream().anyMatch(column -> !column.matches("[A-Z_]+"))) throw new IllegalArgumentException("Configurazione impostazioni non valida."); }
}
