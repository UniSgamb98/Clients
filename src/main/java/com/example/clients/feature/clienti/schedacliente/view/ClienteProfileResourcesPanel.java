package com.example.clients.feature.clienti.schedacliente.view;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Full-width panel reserved for the customer's production resources. */
final class ClienteProfileResourcesPanel extends VBox {

    private static final String[] RESOURCES = {
            "Materiali", "Forni", "Fresatori", "Ceramiche", "Frese", "Canale di vendita"
    };

    ClienteProfileResourcesPanel() {
        super(12);
        getStyleClass().add("client-profile-resources-panel");

        Label title = new Label("Risorse cliente");
        title.getStyleClass().add("client-profile-resources-title");

        FlowPane resources = new FlowPane(12, 12);
        resources.getStyleClass().add("client-profile-resources-flow");
        for (String resource : RESOURCES) {
            resources.getChildren().add(createResourceCard(resource));
        }
        getChildren().addAll(title, resources);
    }

    private VBox createResourceCard(String title) {
        VBox card = new VBox();
        card.getStyleClass().add("client-profile-resource-card");
        Label label = new Label(title);
        label.getStyleClass().add("client-profile-resource-card-title");
        card.getChildren().add(label);
        return card;
    }
}
