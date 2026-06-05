module com.example.clients {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Derby Apache
    requires org.apache.derby.tools;
    requires org.apache.derby.server;
    requires java.sql;

    // App
    exports com.example.clients.app;

    // Core
    exports com.example.clients.core.database;
}