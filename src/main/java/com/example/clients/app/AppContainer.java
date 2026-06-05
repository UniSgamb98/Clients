package com.example.clients.app;

import com.example.clients.core.database.Database;
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
    private final Connection sharedConnection;

    protected AppContainer() {

        // DATABASE
        this.database = new Database();
        database.start();
        this.sharedConnection = database.getConnection();

        // REPOSITORIES
      //  this.itemRepo = new ItemRepositoryImpl(sharedConnection);
        System.out.println("Caricati le repository.");

        // SHARED SERVICES
    }

    // --- PUBLIC GETTERS ---


    public void shutdown() {
        try {
            if (!sharedConnection.isClosed()) {
                sharedConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la chiusura della connessione condivisa.", e);
        }
    }
}
