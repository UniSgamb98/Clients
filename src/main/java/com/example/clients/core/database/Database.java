package com.example.clients.core.database;

import org.apache.derby.drda.NetworkServerControl;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class Database {

    private static final String DB_NAME = "Clients";
    private static final String DB_USER = "APP";
    private static final String DB_PASSWORD = "pw";
    private static final String DERBY_SYSTEM_HOME = "I:\\CliZr\\Tommaso\\";

    private static final int DERBY_PORT = 1527;
    private static final int DISCOVERY_PORT = 45678;

    private String jdbcUrl;

    private DatabaseMode mode = DatabaseMode.NOT_STARTED;

    private NetworkServerControl derbyServer;
    private DiscoveryServer discoveryServer;

    public enum DatabaseMode {
        NOT_STARTED,
        HOST,
        CLIENT
    }

    public void start() {
        System.setProperty("derby.system.home", DERBY_SYSTEM_HOME);

        Optional<HostInfo> existingHost = findExistingHost(1200);

        if (existingHost.isPresent()) {
            startAsClient(existingHost.get());
            return;
        }

        try {
            startAsHost();
            return;
        } catch (Exception hostStartException) {
            System.out.println("Impossibile avviare come HOST. Cerco un server già attivo...");

            Optional<HostInfo> hostAfterFailure = findExistingHost(3000);

            if (hostAfterFailure.isPresent()) {
                startAsClient(hostAfterFailure.get());
                return;
            }

            throw new RuntimeException("Impossibile avviare il database e nessun host trovato in LAN.", hostStartException);
        }
    }

    public Connection getConnection() {
        if (jdbcUrl == null) {
            start();
        }

        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Errore connessione DB: " + jdbcUrl, e);
        }
    }

    private Optional<HostInfo> findExistingHost(int timeoutMs) {
        DiscoveryClient discoveryClient = new DiscoveryClient(DISCOVERY_PORT, timeoutMs);
        return discoveryClient.findHost();
    }

    private void startAsHost() throws Exception {
        System.out.println("Avvio database in modalità HOST...");

        System.setProperty("derby.drda.host", "0.0.0.0");
        System.setProperty("derby.drda.portNumber", String.valueOf(DERBY_PORT));

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        derbyServer = new NetworkServerControl(
                InetAddress.getByName("0.0.0.0"),
                DERBY_PORT
        );

        derbyServer.start(null);

        waitForDerbyServerStart();

        Class.forName("org.apache.derby.jdbc.ClientDriver");

        jdbcUrl = buildJdbcUrl("localhost", true);

        try (Connection ignored = DriverManager.getConnection(jdbcUrl)) {
            // Crea il database se non esiste.
        }

        discoveryServer = new DiscoveryServer(DISCOVERY_PORT, DERBY_PORT);
        Thread discoveryThread = new Thread(discoveryServer, "clizr-discovery-server");
        discoveryThread.setDaemon(true);
        discoveryThread.start();

        mode = DatabaseMode.HOST;

        System.out.println("Database avviato come HOST.");
        System.out.println("JDBC URL: " + jdbcUrl);
    }

    private void startAsClient(HostInfo hostInfo) {
        try {
            System.out.println("Avvio database in modalità CLIENT...");
            System.out.println("Host trovato: " + hostInfo.getIp());

            Class.forName("org.apache.derby.jdbc.ClientDriver");

            jdbcUrl = buildJdbcUrl(hostInfo.getIp(), false);

            try (Connection ignored = DriverManager.getConnection(jdbcUrl)) {
                // Test connessione.
            }

            mode = DatabaseMode.CLIENT;

            System.out.println("Connesso al database remoto.");
            System.out.println("JDBC URL: " + jdbcUrl);

        } catch (Exception e) {
            throw new RuntimeException("Errore connessione al database remoto.", e);
        }
    }

    private String buildJdbcUrl(String host, boolean create) {
        StringBuilder builder = new StringBuilder();

        builder.append("jdbc:derby://")
                .append(host)
                .append(":")
                .append(DERBY_PORT)
                .append("/")
                .append(DB_NAME)
                .append(";user=")
                .append(DB_USER)
                .append(";password=")
                .append(DB_PASSWORD);

        if (create) {
            builder.append(";create=true");
        }

        return builder.toString();
    }

    private void waitForDerbyServerStart() throws Exception {
        NetworkServerControl control = new NetworkServerControl(
                InetAddress.getByName("localhost"),
                DERBY_PORT
        );

        Exception lastException = null;

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(500);
                control.ping();
                return;
            } catch (Exception e) {
                lastException = e;
            }
        }

        throw lastException;
    }

    public void stop() {
        if (discoveryServer != null) {
            discoveryServer.close();
        }

        if (mode == DatabaseMode.HOST && derbyServer != null) {
            try {
                derbyServer.shutdown();
                System.out.println("Derby Network Server chiuso.");
            } catch (Exception e) {
                System.out.println("Errore durante la chiusura del Derby Network Server.");
                e.printStackTrace();
            }
        }

        mode = DatabaseMode.NOT_STARTED;
        jdbcUrl = null;
    }

    public DatabaseMode getMode() {
        return mode;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}