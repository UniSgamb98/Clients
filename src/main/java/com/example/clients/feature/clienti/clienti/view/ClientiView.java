package com.example.clients.feature.clienti.clienti.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.clienti.clienti.service.ClientiService.OperatoreFilter;
import com.example.clients.feature.clienti.clienti.service.ClientiService.TextFilter;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

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
    private final ChoiceBox<OperatoreFilter> operatorFilterChoiceBox;
    private final ChoiceBox<TextFilter> typeFilterChoiceBox;
    private final ChoiceBox<TextFilter> statusFilterChoiceBox;
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
    private final Button previousPageButton;
    private final Button nextPageButton;
    private final Label pageLabel;

    public ClientiView() {
        header = new AppHeader("Clienti");
        sidebar = new AppSidebar();

        searchField = new TextField();
        searchField.setPromptText("Cerca clienti...");
        searchField.getStyleClass().add("clients-search-field");

        newClientButton = new Button("+ Nuovo cliente");
        newClientButton.getStyleClass().add("clients-primary-button");

        operatorFilterChoiceBox = new ChoiceBox<>();
        operatorFilterChoiceBox.getStyleClass().add("clients-operator-filter-choice");
        operatorFilterChoiceBox.getItems().add(OperatoreFilter.empty());
        operatorFilterChoiceBox.getSelectionModel().selectFirst();
        typeFilterChoiceBox = createTextFilterChoiceBox("Tutti i tipi cliente");
        statusFilterChoiceBox = createTextFilterChoiceBox("Tutti gli stati trattativa");

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

        previousPageButton = createFilterButton("‹ Indietro");
        nextPageButton = createFilterButton("Avanti ›");
        pageLabel = new Label("Pagina -");
        pageLabel.getStyleClass().add("clients-pagination-label");
        setPaginationDisabled(true);

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
        Label operatorFilterLabel = createFilterLabel("Operatore");
        Label typeFilterLabel = createFilterLabel("Tipo cliente");
        Label statusFilterLabel = createFilterLabel("Stato trattativa");
        filters.getChildren().addAll(
                operatorFilterLabel, operatorFilterChoiceBox,
                typeFilterLabel, typeFilterChoiceBox,
                statusFilterLabel, statusFilterChoiceBox
        );

        initializeTable();
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(tableScrollPane, javafx.scene.layout.Priority.ALWAYS);

        content.getChildren().addAll(titleBar, toolbar, filters, table, createPaginationBar());
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

    private HBox createPaginationBar() {
        HBox pagination = new HBox(10);
        pagination.getStyleClass().add("clients-pagination-bar");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        pagination.getChildren().addAll(spacer, previousPageButton, pageLabel, nextPageButton);
        return pagination;
    }

    private Label createFilterLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("clients-filter-label");
        return label;
    }

    private ChoiceBox<TextFilter> createTextFilterChoiceBox(String emptyLabel) {
        ChoiceBox<TextFilter> choiceBox = new ChoiceBox<>();
        choiceBox.getStyleClass().add("clients-operator-filter-choice");
        choiceBox.getItems().add(TextFilter.empty(emptyLabel));
        choiceBox.getSelectionModel().selectFirst();
        return choiceBox;
    }

    private HBox createEmptyRow() {
        return createMessageRow("Nessun cliente caricato. Usa \"+ Nuovo cliente\" per iniziare.");
    }

    private HBox createMessageRow(String message) {
        HBox row = new HBox();
        row.getStyleClass().add("clients-empty-row");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("clients-empty-label");
        messageLabel.setWrapText(true);
        row.getChildren().add(messageLabel);
        return row;
    }

    public void showLoading() {
        showMessage("Caricamento clienti...");
    }

    public void showEmpty() {
        showMessage("Nessun cliente trovato.");
    }

    public void showError(String message) {
        showMessage(message == null || message.isBlank() ? "Caricamento clienti non riuscito." : message);
    }

    private void showMessage(String message) {
        tableRows.getChildren().clear();
        tableRows.getChildren().add(createMessageRow(message));
        tableScrollPane.setVvalue(0);
    }

    public void clearClientRows() {
        tableRows.getChildren().clear();
        tableRows.getChildren().add(emptyRow);
        tableScrollPane.setVvalue(0);
    }

    public void renderPagination(int page, int totalPages, boolean hasPreviousPage, boolean hasNextPage) {
        pageLabel.setText(totalPages == 0 ? "Nessuna pagina" : "Pagina " + (page + 1) + " di " + totalPages);
        previousPageButton.setDisable(!hasPreviousPage);
        nextPageButton.setDisable(!hasNextPage);
    }

    public void setPaginationDisabled(boolean disabled) {
        previousPageButton.setDisable(disabled);
        nextPageButton.setDisable(disabled);
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

    public ChoiceBox<OperatoreFilter> getOperatorFilterChoiceBox() {
        return operatorFilterChoiceBox;
    }

    public ChoiceBox<TextFilter> getTypeFilterChoiceBox() {
        return typeFilterChoiceBox;
    }

    public ChoiceBox<TextFilter> getStatusFilterChoiceBox() {
        return statusFilterChoiceBox;
    }

    public void setTypeFilters(List<TextFilter> types) {
        setTextFilters(typeFilterChoiceBox, TextFilter.empty("Tutti i tipi cliente"), types);
    }

    public void setStatusFilters(List<TextFilter> statuses) {
        setTextFilters(statusFilterChoiceBox, TextFilter.empty("Tutti gli stati trattativa"), statuses);
    }

    private void setTextFilters(ChoiceBox<TextFilter> choiceBox, TextFilter emptyFilter, List<TextFilter> filters) {
        TextFilter selected = choiceBox.getSelectionModel().getSelectedItem();
        choiceBox.getItems().setAll(emptyFilter);
        if (filters != null) {
            choiceBox.getItems().addAll(filters);
        }

        if (selected != null && choiceBox.getItems().contains(selected)) {
            choiceBox.getSelectionModel().select(selected);
        } else {
            choiceBox.getSelectionModel().selectFirst();
        }
    }

    public void setOperatorFilters(List<OperatoreFilter> operators) {
        OperatoreFilter selected = operatorFilterChoiceBox.getSelectionModel().getSelectedItem();
        operatorFilterChoiceBox.getItems().setAll(OperatoreFilter.empty());
        if (operators != null) {
            operatorFilterChoiceBox.getItems().addAll(operators);
        }

        if (selected != null && operatorFilterChoiceBox.getItems().contains(selected)) {
            operatorFilterChoiceBox.getSelectionModel().select(selected);
        } else {
            operatorFilterChoiceBox.getSelectionModel().selectFirst();
        }
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

    public Button getPreviousPageButton() {
        return previousPageButton;
    }

    public Button getNextPageButton() {
        return nextPageButton;
    }
}
