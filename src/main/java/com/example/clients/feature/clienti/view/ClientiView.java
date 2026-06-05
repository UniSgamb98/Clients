package com.example.clients.feature.clienti.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClientiView extends BorderPane {

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final TextField searchField;
    private final Button newClientButton;
    private final Button allFilterButton;
    private final Button activeFilterButton;
    private final Button prospectFilterButton;
    private final Button inactiveFilterButton;
    private final VBox table;
    private final HBox emptyRow;

    public ClientiView() {
        header = new AppHeader("Clienti");
        sidebar = new AppSidebar();

        searchField = new TextField();
        searchField.setPromptText("Cerca clienti...");
        searchField.getStyleClass().add("clients-search-field");

        newClientButton = new Button("+ Nuovo cliente");
        newClientButton.getStyleClass().add("clients-primary-button");

        allFilterButton = createFilterButton("Tutti");
        activeFilterButton = createFilterButton("Attivi");
        prospectFilterButton = createFilterButton("Prospect");
        inactiveFilterButton = createFilterButton("Inattivi");

        table = new VBox();
        table.getStyleClass().add("clients-table");
        emptyRow = createEmptyRow();

        setTop(header);
        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("clients-content");

        HBox titleBar = new HBox(12);
        titleBar.getStyleClass().add("clients-title-bar");

        VBox titleBox = new VBox(4);
        Label title = new Label("Clienti");
        title.getStyleClass().add("clients-title");
        Label subtitle = new Label("Gestisci anagrafiche, riferimenti e informazioni commerciali dei clienti.");
        subtitle.getStyleClass().add("clients-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        HBox titleSpacer = new HBox();
        HBox.setHgrow(titleSpacer, javafx.scene.layout.Priority.ALWAYS);
        titleBar.getChildren().addAll(titleBox, titleSpacer, newClientButton);

        HBox toolbar = new HBox(10);
        toolbar.getStyleClass().add("clients-toolbar");
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, javafx.scene.layout.Priority.ALWAYS);
        toolbar.getChildren().add(searchField);

        HBox filters = new HBox(8);
        filters.getStyleClass().add("clients-filter-bar");
        filters.getChildren().addAll(allFilterButton, activeFilterButton, prospectFilterButton, inactiveFilterButton);

        initializeTable();

        content.getChildren().addAll(titleBar, toolbar, filters, table);
        return content;
    }

    private void initializeTable() {
        HBox headerRow = createTableRow("Nome", "Tipo", "Referente", "Telefono", "Email", "Stato");
        headerRow.getStyleClass().add("clients-table-header");
        table.getChildren().addAll(headerRow, emptyRow);
    }

    private HBox createEmptyRow() {
        HBox row = new HBox();
        row.getStyleClass().add("clients-empty-row");
        Label emptyLabel = new Label("Nessun cliente caricato. Usa \"+ Nuovo cliente\" per iniziare.");
        emptyLabel.getStyleClass().add("clients-empty-label");
        row.getChildren().add(emptyLabel);
        return row;
    }

    public void clearClientRows() {
        while (table.getChildren().size() > 1) {
            table.getChildren().remove(1);
        }

        table.getChildren().add(emptyRow);
    }

    public void addClientRow(String name, String type, String contact, String phone, String email, String status) {
        table.getChildren().remove(emptyRow);
        HBox row = createTableRow(name, type, contact, phone, email, status);
        row.getStyleClass().add("clients-data-row");
        table.getChildren().add(row);
    }

    private HBox createTableRow(String name, String type, String contact, String phone, String email, String status) {
        HBox row = new HBox();
        row.getStyleClass().add("clients-table-row");
        row.getChildren().addAll(
                createCell(name),
                createCell(type),
                createCell(contact),
                createCell(phone),
                createCell(email),
                createCell(status)
        );
        return row;
    }

    private Label createCell(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("clients-table-cell");
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, javafx.scene.layout.Priority.ALWAYS);
        return label;
    }

    private Button createFilterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("clients-filter-button");
        return button;
    }

    public AppHeader getHeader() {
        return header;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getNewClientButton() {
        return newClientButton;
    }

    public Button getAllFilterButton() {
        return allFilterButton;
    }

    public Button getActiveFilterButton() {
        return activeFilterButton;
    }

    public Button getProspectFilterButton() {
        return prospectFilterButton;
    }

    public Button getInactiveFilterButton() {
        return inactiveFilterButton;
    }
}
