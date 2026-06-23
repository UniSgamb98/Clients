package com.example.clients.feature.clienti.schedacliente.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

final class ClienteDataSection extends VBox {

    private final VBox content;

    ClienteDataSection() {
        super(12);
        getStyleClass().add("new-client-section");
        Label title = new Label("Dati cliente");
        title.getStyleClass().add("new-client-section-title");
        content = new VBox(8);
        getChildren().addAll(title, content);
    }

    VBox getContent() {
        return content;
    }

    void setContent(Node... nodes) {
        content.getChildren().setAll(nodes);
    }
}
