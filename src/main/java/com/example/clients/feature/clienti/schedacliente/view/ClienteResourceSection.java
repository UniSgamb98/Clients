package com.example.clients.feature.clienti.schedacliente.view;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Reusable resource section with a title, add button and card list. */
final class ClienteResourceSection extends VBox {

    private final Button addButton;
    private final VBox list = new VBox(10);

    ClienteResourceSection(String titleText, String addButtonText) {
        super(10);
        getStyleClass().add("client-profile-resource-group");

        HBox header = new HBox(10);
        header.getStyleClass().add("client-profile-resource-group-header");
        Label title = new Label(titleText);
        title.getStyleClass().add("client-profile-resource-card-title");

        addButton = new Button(addButtonText);
        addButton.getStyleClass().add("clients-primary-button");
        header.getChildren().addAll(title, addButton);

        getChildren().addAll(header, list);
    }

    Button addButton() {
        return addButton;
    }

    void setAddButtonVisible(boolean visible) {
        addButton.setVisible(visible);
        addButton.setManaged(visible);
    }

    void clearCards() {
        list.getChildren().clear();
    }

    void addCard(Node card) {
        list.getChildren().add(card);
    }

    void removeCard(Node card) {
        list.getChildren().remove(card);
    }
}
