package com.example.clients.feature.attivita.view;

import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.attivita.service.AttivitaService.AttivitaItem;
import com.example.clients.feature.attivita.service.AttivitaService.ClienteItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AttivitaView extends BorderPane {

    private final AppSidebar sidebar;
    private final Button newActivityButton;
    private final ListView<AttivitaItem> activitiesListView;
    private final VBox rulesPlaceholder;
    private final TextField searchClientField;
    private final ListView<ClienteItem> availableClientsListView;
    private final ListView<ClienteItem> selectedClientsListView;
    private final Button addClientButton;
    private final Button removeClientButton;
    private final Label selectedActivityTitle;

    public AttivitaView() {
        sidebar = new AppSidebar();
        newActivityButton = new Button("+ Nuova attività");
        newActivityButton.getStyleClass().add("clients-primary-button");
        activitiesListView = new ListView<>();
        activitiesListView.getStyleClass().add("activities-list");
        rulesPlaceholder = createRulesPlaceholder();
        searchClientField = new TextField();
        searchClientField.setPromptText("Cerca per ragione sociale...");
        searchClientField.getStyleClass().add("activities-search-field");
        availableClientsListView = createClientListView();
        selectedClientsListView = createClientListView();
        addClientButton = createMoveButton("Aggiungi →");
        removeClientButton = createMoveButton("← Rimuovi");
        selectedActivityTitle = new Label("Seleziona un'attività");
        selectedActivityTitle.getStyleClass().add("activities-selected-title");

        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("activities-content");
        content.getChildren().addAll(createTitleBar(), createActivitiesSection(), createClientsSection());
        return content;
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Attività");
        title.getStyleClass().add("activities-title");
        Label subtitle = new Label("Gestisci attività e lista clienti associati.");
        subtitle.getStyleClass().add("activities-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleBox, spacer, newActivityButton);
        return titleBar;
    }

    private VBox createActivitiesSection() {
        VBox section = createCard();
        Label title = new Label("Elenco attività");
        title.getStyleClass().add("activities-section-title");
        activitiesListView.setPrefHeight(150);
        section.getChildren().addAll(title, activitiesListView);
        return section;
    }

    private VBox createClientsSection() {
        VBox section = createCard();
        Label title = new Label("Clienti attività");
        title.getStyleClass().add("activities-section-title");
        section.getChildren().addAll(title, selectedActivityTitle, rulesPlaceholder, createDualListBox());
        VBox.setVgrow(section, Priority.ALWAYS);
        return section;
    }

    private HBox createDualListBox() {
        HBox dualList = new HBox(12);
        dualList.getStyleClass().add("activities-dual-list");
        dualList.getChildren().addAll(createAvailableClientsBox(), createMoveActions(), createSelectedClientsBox());
        VBox.setVgrow(dualList, Priority.ALWAYS);
        return dualList;
    }

    private VBox createAvailableClientsBox() {
        VBox box = new VBox(8);
        Label title = new Label("Tutti i clienti");
        title.getStyleClass().add("activities-list-title");
        box.getChildren().addAll(title, searchClientField, availableClientsListView);
        HBox.setHgrow(box, Priority.ALWAYS);
        VBox.setVgrow(availableClientsListView, Priority.ALWAYS);
        return box;
    }

    private VBox createSelectedClientsBox() {
        VBox box = new VBox(8);
        Label title = new Label("Clienti aggiunti");
        title.getStyleClass().add("activities-list-title");
        box.getChildren().addAll(title, selectedClientsListView);
        HBox.setHgrow(box, Priority.ALWAYS);
        VBox.setVgrow(selectedClientsListView, Priority.ALWAYS);
        return box;
    }

    private VBox createMoveActions() {
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.getChildren().addAll(addClientButton, removeClientButton);
        return actions;
    }

    private VBox createRulesPlaceholder() {
        VBox placeholder = new VBox(6);
        placeholder.getStyleClass().add("activities-rules-placeholder");
        Label title = new Label("Regole di filtraggio");
        title.getStyleClass().add("activities-rules-title");
        Label text = new Label("Spazio riservato ai filtri automatici: stato trattativa, tipo cliente, coinvolgimento e altre regole.");
        text.getStyleClass().add("activities-rules-text");
        text.setWrapText(true);
        placeholder.getChildren().addAll(title, text);
        return placeholder;
    }

    private VBox createCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("activities-card");
        return card;
    }

    private ListView<ClienteItem> createClientListView() {
        ListView<ClienteItem> listView = new ListView<>();
        listView.getStyleClass().add("activities-client-list");
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(ClienteItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        return listView;
    }

    private Button createMoveButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("clients-filter-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getNewActivityButton() {
        return newActivityButton;
    }

    public ListView<AttivitaItem> getActivitiesListView() {
        return activitiesListView;
    }

    public TextField getSearchClientField() {
        return searchClientField;
    }

    public ListView<ClienteItem> getAvailableClientsListView() {
        return availableClientsListView;
    }

    public ListView<ClienteItem> getSelectedClientsListView() {
        return selectedClientsListView;
    }

    public Button getAddClientButton() {
        return addClientButton;
    }

    public Button getRemoveClientButton() {
        return removeClientButton;
    }

    public Label getSelectedActivityTitle() {
        return selectedActivityTitle;
    }
}
