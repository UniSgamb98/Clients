package com.example.clients.feature.impostazioni.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.feature.impostazioni.dto.Forno;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImpostazioniService {
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
}
