package com.example.clients.feature.clienti.schedacliente.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

final class ClienteRelatedSections extends ResponsiveTilePane {

    private final VBox contactsList;
    private final VBox addressesList;

    ClienteRelatedSections(double gap, double twoColumnBreakpoint) {
        super(gap, twoColumnBreakpoint);
        getStyleClass().add("client-profile-related-sections-grid");
        contactsList = new VBox(8);
        addressesList = new VBox(8);
        addStretchingTile(createSection("Contatti", contactsList));
        addStretchingTile(createSection("Indirizzi", addressesList));
    }

    VBox getContactsList() {
        return contactsList;
    }

    VBox getAddressesList() {
        return addressesList;
    }

    private VBox createSection(String titleText, Node body) {
        VBox section = new VBox(12);
        section.getStyleClass().add("new-client-section");
        Label title = new Label(titleText);
        title.getStyleClass().add("new-client-section-title");
        section.getChildren().addAll(title, body);
        return section;
    }
}
