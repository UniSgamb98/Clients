package com.example.clients.feature.clienti.clienti.view;

import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.clienti.clienti.dto.ClientePreview;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;
import com.example.clients.feature.clienti.clienti.dto.SortColumn;
import com.example.clients.feature.clienti.clienti.dto.TextFilter;
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
import java.util.function.Consumer;

public class ClientiView extends BorderPane {

    private static final double NAME_COLUMN_WIDTH = 190;
    private static final double TYPE_COLUMN_WIDTH = 105;
    private static final double CONTACT_COLUMN_WIDTH = 135;
    private static final double OPERATOR_COLUMN_WIDTH = 130;
    private static final double STATUS_COLUMN_WIDTH = 105;
    private static final double LAST_CONTACT_COLUMN_WIDTH = 125;
    private static final double ACTIONS_COLUMN_WIDTH = 80;

    private final AppSidebar sidebar;
    private final TextField searchField;
    private final Button newClientButton;
    private final ChoiceBox<OperatoreFilter> operatorFilterChoiceBox;
    private final ChoiceBox<TextFilter> typeFilterChoiceBox;
    private final ChoiceBox<TextFilter> statusFilterChoiceBox;
    private final Button otherFiltersButton;
    private final Button clearFiltersButton;
    private final Button saveSearchButton;
    private final Label resultsCountLabel;
    private final Button nameHeaderButton;
    private final Button typeHeaderButton;
    private final Button contactHeaderButton;
    private final Button operatorHeaderButton;
    private final Button statusHeaderButton;
    private final Button lastContactHeaderButton;
    private final VBox table;
    private final HBox resultsArea;
    private final VBox tableRows;
    private final HBox emptyRow;
    private final ScrollPane tableScrollPane;
    private final ClientePreviewDetailPanel detailPanel;
    private final ClientiResultsTable resultsTable;
    private HBox selectedClientRow;

    public ClientiView() {
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
        typeFilterChoiceBox = createTextFilterChoiceBox("Tutti");
        statusFilterChoiceBox = createTextFilterChoiceBox("Tutti");
        otherFiltersButton = new Button("Altri filtri");
        otherFiltersButton.getStyleClass().add("clients-other-filters-button");
        clearFiltersButton = new Button("Pulisci filtri");
        clearFiltersButton.getStyleClass().add("clients-clear-filters-button");
        saveSearchButton = new Button("Salva ricerca");
        saveSearchButton.getStyleClass().add("clients-save-search-button");
        resultsCountLabel = new Label("0 risultati trovati");
        resultsCountLabel.getStyleClass().add("clients-results-count");

        nameHeaderButton = createHeaderButton("Ragione sociale", NAME_COLUMN_WIDTH);
        typeHeaderButton = createHeaderButton("Tipo", TYPE_COLUMN_WIDTH);
        contactHeaderButton = createHeaderButton("Referente", CONTACT_COLUMN_WIDTH);
        operatorHeaderButton = createHeaderButton("Operatore", OPERATOR_COLUMN_WIDTH);
        statusHeaderButton = createHeaderButton("Stato", STATUS_COLUMN_WIDTH);
        lastContactHeaderButton = createHeaderButton("Ultimo contatto", LAST_CONTACT_COLUMN_WIDTH);

        table = new VBox();
        table.getStyleClass().add("clients-table");
        tableRows = new VBox();
        emptyRow = createEmptyRow();
        tableScrollPane = new ScrollPane(tableRows);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.getStyleClass().add("clients-table-scroll");
        detailPanel = new ClientePreviewDetailPanel(this::closeClientDetails);
        detailPanel.setManaged(false);
        detailPanel.setVisible(false);
        resultsArea = new HBox(16, table, detailPanel);
        resultsArea.getStyleClass().add("clients-results-area");
        HBox.setHgrow(table, javafx.scene.layout.Priority.ALWAYS);

        resultsTable = new ClientiResultsTable();

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

        HBox filters = new HBox(12);
        filters.getStyleClass().add("clients-filter-bar");
        VBox searchFilter = createFilterControl("Ricerca", searchField);
        searchFilter.getStyleClass().add("clients-search-filter-control");
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchFilter, javafx.scene.layout.Priority.ALWAYS);
        filters.getChildren().addAll(
                searchFilter,
                createFilterControl("Stato cliente", statusFilterChoiceBox),
                createFilterControl("Operatore", operatorFilterChoiceBox),
                createFilterControl("Tipologia", typeFilterChoiceBox),
                otherFiltersButton
        );

        initializeTable();
        VBox.setVgrow(resultsTable, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(resultsArea, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(tableScrollPane, javafx.scene.layout.Priority.ALWAYS);

        content.getChildren().addAll(titleBar, filters, createFilterActionsBar(), resultsTable);
        return content;
    }

    private VBox createFilterControl(String labelText, Region control) {
        VBox filterControl = new VBox(4);
        filterControl.getStyleClass().add("clients-filter-control");
        filterControl.getChildren().addAll(createFilterLabel(labelText), control);
        return filterControl;
    }

    private HBox createFilterActionsBar() {
        HBox actions = new HBox(12);
        actions.getStyleClass().add("clients-filter-actions");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        actions.getChildren().addAll(resultsCountLabel, spacer, clearFiltersButton, saveSearchButton);
        return actions;
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
                operatorHeaderButton,
                statusHeaderButton,
                lastContactHeaderButton,
                createActionsHeader()
        );
        return row;
    }

    private Label createActionsHeader() {
        Label header = new Label("Azioni");
        header.getStyleClass().addAll("clients-table-cell", "clients-actions-header");
        setColumnWidth(header, ACTIONS_COLUMN_WIDTH);
        return header;
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
        resultsTable.showLoading();
    }

    public void showEmpty() {
        resultsTable.showEmpty();
    }

    public void showError(String message) {
        resultsTable.showError(message);
    }

    private void showMessage(String message) {
        closeClientDetails();
        tableRows.getChildren().clear();
        tableRows.getChildren().add(createMessageRow(message));
        tableScrollPane.setVvalue(0);
    }

    public void clearClientRows() {
        resultsTable.clearRows();
    }

    public void setResultsCount(long totalResults) {
        resultsCountLabel.setText(totalResults + (totalResults == 1 ? " risultato trovato" : " risultati trovati"));
    }

    public void clearFilters() {
        searchField.clear();
        operatorFilterChoiceBox.getSelectionModel().selectFirst();
        typeFilterChoiceBox.getSelectionModel().selectFirst();
        statusFilterChoiceBox.getSelectionModel().selectFirst();
    }

    public HBox addClientRow(String name, String type, String contact, String operator, String status, String lastContact, Runnable onActionsClick) {
        return resultsTable.addClientRow(name, type, contact, operator, status, lastContact, onActionsClick);
    }

    public void openClientDetails(ClientePreview preview, HBox row, Runnable onOpenProfile) {
        resultsTable.openClientDetails(preview, row, onOpenProfile);
    }

    public void closeClientDetails() {
        resultsTable.closeClientDetails();
    }

    private HBox createTableRow(String name, String type, String contact, String operator, String status, String lastContact, Runnable onActionsClick) {
        HBox row = new HBox();
        row.getStyleClass().add("clients-table-row");
        row.getChildren().addAll(
                createCell(name, NAME_COLUMN_WIDTH),
                createCell(type, TYPE_COLUMN_WIDTH),
                createCell(contact, CONTACT_COLUMN_WIDTH),
                createCell(operator, OPERATOR_COLUMN_WIDTH),
                createCell(status, STATUS_COLUMN_WIDTH),
                createCell(lastContact, LAST_CONTACT_COLUMN_WIDTH),
                createActionsButton(onActionsClick)
        );
        return row;
    }

    private Label createCell(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("clients-table-cell");
        setColumnWidth(label, width);
        return label;
    }

    private Button createActionsButton(Runnable onActionsClick) {
        Button button = new Button("...");
        button.getStyleClass().add("clients-row-actions-button");
        setColumnWidth(button, ACTIONS_COLUMN_WIDTH);
        button.setOnAction(event -> {
            event.consume();
            onActionsClick.run();
        });
        return button;
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


    public void setTypeFilters(List<TextFilter> types) {
        setTextFilters(typeFilterChoiceBox, TextFilter.empty("Tutti"), types);
    }

    public void setStatusFilters(List<TextFilter> statuses) {
        setTextFilters(statusFilterChoiceBox, TextFilter.empty("Tutti"), statuses);
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

    public void onNewClient(Runnable action) {
        newClientButton.setOnAction(event -> action.run());
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public void onSortRequested(Consumer<SortColumn> action) {
        resultsTable.onSortRequested(action);
    }

    public void onSearchChanged(Consumer<String> action) {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> action.accept(newValue));
    }

    public void onOperatoreFilterChanged(Consumer<OperatoreFilter> action) {
        operatorFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> action.accept(newValue));
    }

    public void onTipologiaFilterChanged(Consumer<TextFilter> action) {
        typeFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> action.accept(newValue));
    }

    public void onStatoFilterChanged(Consumer<TextFilter> action) {
        statusFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> action.accept(newValue));
    }

    public void onClearFilters(Runnable action) {
        clearFiltersButton.setOnAction(event -> action.run());
    }

    public void onSaveSearch(Runnable action) {
        saveSearchButton.setOnAction(event -> action.run());
    }
}
