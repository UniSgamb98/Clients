package com.example.clients.feature.clienti.clienti.view;

import javafx.scene.control.Alert;

public class ClientiFeedback {

    public void showFeatureInDevelopment(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Funzionalità in sviluppo");
        alert.setHeaderText(feature);
        alert.setContentText("Questa funzionalità sarà disponibile prossimamente.");
        alert.showAndWait();
    }
}
