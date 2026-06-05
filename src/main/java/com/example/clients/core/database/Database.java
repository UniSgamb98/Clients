package com.example.clients.core.database;

import org.apache.derby.drda.NetworkServerControl;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public final class Database {

    private static final String DB_NAME = "Clients";
    private static final String DB_USER = "APP";
    private static final String DB_PASSWORD = "pw";
    private static final String DERBY_SYSTEM_HOME = Path.of(System.getProperty("user.home"), ".clizr", "derby").toString();

    private static final int DERBY_PORT = 1527;
    private static final int DISCOVERY_PORT = 45678;
    private static final int DISCOVERY_TIMEOUT_MS = 1500;

    private String jdbcUrl;
    private DatabaseMode mode = DatabaseMode.NOT_STARTED;
    private NetworkServerControl derbyServer;
    private DiscoveryServer discoveryServer;

    public enum DatabaseMode {
        NOT_STARTED,
        HOST,
        CLIENT
    }

    public synchronized void start() {
        if (jdbcUrl != null) {
            return;
        }

        configureDerbyHome();

        Optional<HostInfo> host = findExistingHost();

        if (host.isPresent()) {
            startAsClient(host.get());
            return;
        }

        try {
            startAsHost();
        } catch (Exception e) {
            Optional<HostInfo> hostAfterFailure = findExistingHost();

            if (hostAfterFailure.isPresent()) {
                startAsClient(hostAfterFailure.get());
                return;
            }

            throw new RuntimeException("Impossibile avviare il database e nessun host trovato in LAN.", e);
        }
    }

    public synchronized Connection getConnection() {
        if (jdbcUrl == null) {
            start();
        }

        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Errore connessione DB: " + jdbcUrl, e);
        }
    }

    private Optional<HostInfo> findExistingHost() {
        return new DiscoveryClient(DISCOVERY_PORT, DISCOVERY_TIMEOUT_MS).findHost();
    }

    private void startAsHost() throws Exception {
        System.out.println("Avvio database in modalità HOST...");

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Class.forName("org.apache.derby.jdbc.ClientDriver");

        derbyServer = new NetworkServerControl(InetAddress.getByName("0.0.0.0"), DERBY_PORT);
        derbyServer.start(null);
        waitForDerbyServer();

        jdbcUrl = buildJdbcUrl("localhost", DERBY_PORT, true);

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

    private void startAsClient(HostInfo host) {
        try {
            System.out.println("Avvio database in modalità CLIENT...");
            System.out.println("Host trovato: " + host.getIp() + ":" + host.getDbPort());

            Class.forName("org.apache.derby.jdbc.ClientDriver");

            jdbcUrl = buildJdbcUrl(host.getIp(), host.getDbPort(), false);

            try (Connection ignored = DriverManager.getConnection(jdbcUrl)) {
                // Verifica la connessione al server remoto.
            }

            mode = DatabaseMode.CLIENT;

            System.out.println("Connesso al database remoto.");
            System.out.println("JDBC URL: " + jdbcUrl);
        } catch (Exception e) {
            throw new RuntimeException("Errore connessione al database remoto.", e);
        }
    }

    private String buildJdbcUrl(String host, int port, boolean create) {
        String url = "jdbc:derby://" + host + ":" + port + "/" + DB_NAME
                + ";user=" + DB_USER
                + ";password=" + DB_PASSWORD;

        if (create) {
            url += ";create=true";
        }

        return url;
    }

    private void waitForDerbyServer() throws Exception {
        NetworkServerControl control = new NetworkServerControl(InetAddress.getByName("localhost"), DERBY_PORT);
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

    public synchronized void stop() {
        if (discoveryServer != null) {
            discoveryServer.close();
            discoveryServer = null;
        }

        if (mode == DatabaseMode.HOST && derbyServer != null) {
            try {
                derbyServer.shutdown();
                System.out.println("Derby Network Server chiuso.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        derbyServer = null;
        jdbcUrl = null;
        mode = DatabaseMode.NOT_STARTED;
    }

    public synchronized DatabaseMode getMode() {
        return mode;
    }

    public synchronized String getJdbcUrl() {
        return jdbcUrl;
    }

    private void configureDerbyHome() {
        try {
            Files.createDirectories(Path.of(DERBY_SYSTEM_HOME));
            System.setProperty("derby.system.home", DERBY_SYSTEM_HOME);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile configurare Derby home: " + DERBY_SYSTEM_HOME, e);
        }
    }
}
