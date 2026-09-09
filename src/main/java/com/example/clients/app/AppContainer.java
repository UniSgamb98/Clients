package com.example.clients.app;

import com.example.clients.core.database.Database;
import com.example.clients.core.session.FeatureSessionStateStore;
//import com.example.clients.core.database.implementation.*;
//import com.example.clients.core.database.repository.*;

import java.sql.Connection;
import java.sql.SQLException;

public class AppContainer {
    // --- Repositories ---
    //private final ItemRepository itemRepo;

    // --- Shared services ---

    // --- Database ---
    protected final Database database;
    protected final FeatureSessionStateStore featureSessionStateStore;
    private final Connection sharedConnection;

    protected AppContainer() {

        // DATABASE
        this.database = new Database();
        database.start();
        this.sharedConnection = database.getConnection();
        this.featureSessionStateStore = new FeatureSessionStateStore();

        // REPOSITORIES
      //  this.itemRepo = new ItemRepositoryImpl(sharedConnection);
        System.out.println("Caricati le repository.");

        // SHARED SERVICES
    }

    // --- PUBLIC GETTERS ---


    public void shutdown() {
        featureSessionStateStore.clear();
        try {
            if (!sharedConnection.isClosed()) {
                sharedConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la chiusura della connessione condivisa.", e);
        }
    }
}
