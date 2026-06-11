package com.example.clients.feature.clienti.clienti.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ClientiView extends BorderPane {

    private static final double NAME_COLUMN_WIDTH = 115;
    private static final double TYPE_COLUMN_WIDTH = 75;
    private static final double CONTACT_COLUMN_WIDTH = 115;
    private static final double PHONE_COLUMN_WIDTH = 90;
    private static final double EMAIL_COLUMN_WIDTH = 175;
    private static final double STATUS_COLUMN_WIDTH = 75;

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final TextField searchField;
    private final Button newClientButton;
    private final Button allFilterButton;
    private final Button activeFilterButton;
    private final Button prospectFilterButton;
    private final Button inactiveFilterButton;
    private final Button nameHeaderButton;
    private final Button typeHeaderButton;
    private final Button contactHeaderButton;
    private final Button phoneHeaderButton;
    private final Button emailHeaderButton;
    private final Button statusHeaderButton;
    private final VBox table;
    private final VBox tableRows;
    private final HBox emptyRow;
    private final ScrollPane tableScrollPane;

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

        nameHeaderButton = createHeaderButton("Nome", NAME_COLUMN_WIDTH);
        typeHeaderButton = createHeaderButton("Tipo", TYPE_COLUMN_WIDTH);
        contactHeaderButton = createHeaderButton("Referente", CONTACT_COLUMN_WIDTH);
        phoneHeaderButton = createHeaderButton("Telefono", PHONE_COLUMN_WIDTH);
        emailHeaderButton = createHeaderButton("Email", EMAIL_COLUMN_WIDTH);
        statusHeaderButton = createHeaderButton("Stato", STATUS_COLUMN_WIDTH);

        table = new VBox();
        table.getStyleClass().add("clients-table");
        tableRows = new VBox();
        emptyRow = createEmptyRow();
        tableScrollPane = new ScrollPane(tableRows);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.getStyleClass().add("clients-table-scroll");

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
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(tableScrollPane, javafx.scene.layout.Priority.ALWAYS);

        content.getChildren().addAll(titleBar, toolbar, filters, table);
        return content;
    }

    private void initializeTable() {
        HBox headerRow = createHeaderRow();
        tableRows.getChildren().add(emptyRow);
        table.getChildren().addAll(headerRow, tableScrollPane);
    }

    private HBox createHeaderRow() {
        HBox row = new HBox();
        row.getStyleClass().add("clients-table-row");
        row.getStyleClass().add("clients-table-header");
        row.getChildren().addAll(
                nameHeaderButton,
                typeHeaderButton,
                contactHeaderButton,
                phoneHeaderButton,
                emailHeaderButton,
                statusHeaderButton
        );
        return row;
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
        tableRows.getChildren().clear();
        tableRows.getChildren().add(emptyRow);
        tableScrollPane.setVvalue(0);
    }

    public HBox addClientRow(String name, String type, String contact, String phone, String email, String status) {
        tableRows.getChildren().remove(emptyRow);
        HBox row = createTableRow(name, type, contact, phone, email, status);
        row.getStyleClass().add("clients-data-row");
        tableRows.getChildren().add(row);
        return row;
    }

    private HBox createTableRow(String name, String type, String contact, String phone, String email, String status) {
        HBox row = new HBox();
        row.getStyleClass().add("clients-table-row");
        row.getChildren().addAll(
                createCell(name, NAME_COLUMN_WIDTH),
                createCell(type, TYPE_COLUMN_WIDTH),
                createCell(contact, CONTACT_COLUMN_WIDTH),
                createCell(phone, PHONE_COLUMN_WIDTH),
                createCell(email, EMAIL_COLUMN_WIDTH),
                createCell(status, STATUS_COLUMN_WIDTH)
        );
        return row;
    }

    private Label createCell(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("clients-table-cell");
        setColumnWidth(label, width);
        return label;
    }

    private Button createFilterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("clients-filter-button");
        return button;
    }

    private Button createHeaderButton(String text, double width) {
        Button button = new Button(text);
        button.getStyleClass().add("clients-table-header-button");
        setColumnWidth(button, width);
        return button;
    }

    private void setColumnWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
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

    public Button getNameHeaderButton() {
        return nameHeaderButton;
    }

    public Button getTypeHeaderButton() {
        return typeHeaderButton;
    }

    public Button getContactHeaderButton() {
        return contactHeaderButton;
    }

    public Button getPhoneHeaderButton() {
        return phoneHeaderButton;
    }

    public Button getEmailHeaderButton() {
        return emailHeaderButton;
    }

    public Button getStatusHeaderButton() {
        return statusHeaderButton;
    }
}
