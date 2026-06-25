package com.example.clients.feature.attivita.view;

import com.example.clients.core.database.query.AttivitaQuery.AttivitaClienteRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaDetailRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaListRecord;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class AttivitaView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppSidebar sidebar;
    private final Button newActivityButton;
    private final VBox activityRows;
    private final ScrollPane activityScrollPane;
    private final VBox detailPanel;
    private final Label detailTitle;
    private final Label detailDescription;
    private final GridPane detailMetaGrid;
    private final VBox detailClientRows;
    private final Label detailEmptyLabel;
    private final HBox detailClientHeader;
    private final TextField titleField;
    private final TextArea descriptionArea;
    private final ChoiceBox<PriorityOption> priorityChoiceBox;
    private final ChoiceBox<StateOption> stateChoiceBox;
    private final ChoiceBox<TipoAttivitaOption> typeChoiceBox;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button saveActivityButton;
    private final Button cancelActivityButton;
    private final Label formMessage;

    public AttivitaView() {
        sidebar = new AppSidebar();
        newActivityButton = new Button("+ Nuova attività");
        newActivityButton.getStyleClass().add("activities-primary-button");

        activityRows = new VBox();
        activityRows.getStyleClass().add("activities-list-rows");
        activityScrollPane = new ScrollPane(activityRows);
        activityScrollPane.setFitToWidth(true);
        activityScrollPane.getStyleClass().add("activities-scroll");

        detailPanel = new VBox(14);
        detailPanel.getStyleClass().add("activities-detail-panel");
        detailTitle = new Label("Seleziona un'attività");
        detailTitle.getStyleClass().add("activities-detail-title");
        detailDescription = new Label("Apri un'attività dalla lista per vedere clienti, stati e ultimo contatto.");
        detailDescription.setWrapText(true);
        detailDescription.getStyleClass().add("activities-detail-description");
        detailMetaGrid = new GridPane();
        detailMetaGrid.getStyleClass().add("activities-detail-meta-grid");
        detailMetaGrid.setHgap(10);
        detailMetaGrid.setVgap(10);
        detailClientRows = new VBox(8);
        detailClientRows.getStyleClass().add("activities-client-rows");
        detailEmptyLabel = new Label("Nessun cliente selezionato.");
        detailEmptyLabel.getStyleClass().add("activities-empty-label");
        detailClientHeader = createClientHeader();

        titleField = new TextField();
        titleField.setPromptText("Es. Avviso chiusura estiva");
        titleField.getStyleClass().add("activities-form-field");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrivi obiettivo, istruzioni e note operative dell'attività...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.getStyleClass().add("activities-form-area");
        priorityChoiceBox = new ChoiceBox<>();
        priorityChoiceBox.getItems().setAll(
                new PriorityOption(1, "Bassa"),
                new PriorityOption(2, "Normale"),
                new PriorityOption(3, "Alta")
        );
        priorityChoiceBox.getSelectionModel().select(1);
        priorityChoiceBox.getStyleClass().add("activities-form-choice");
        stateChoiceBox = new ChoiceBox<>();
        stateChoiceBox.getItems().setAll(
                new StateOption("BOZZA", "Bozza"),
                new StateOption("IN_CORSO", "In corso"),
                new StateOption("SOSPESA", "Sospesa"),
                new StateOption("COMPLETATA", "Completata"),
                new StateOption("ANNULLATA", "Annullata")
        );
        stateChoiceBox.getSelectionModel().selectFirst();
        stateChoiceBox.getStyleClass().add("activities-form-choice");
        typeChoiceBox = new ChoiceBox<>();
        typeChoiceBox.getItems().add(TipoAttivitaOption.empty());
        typeChoiceBox.getSelectionModel().selectFirst();
        typeChoiceBox.getStyleClass().add("activities-form-choice");
        startDatePicker = new DatePicker();
        startDatePicker.getStyleClass().add("activities-form-date");
        endDatePicker = new DatePicker();
        endDatePicker.getStyleClass().add("activities-form-date");
        saveActivityButton = new Button("Salva attività");
        saveActivityButton.getStyleClass().add("activities-primary-button");
        cancelActivityButton = new Button("Annulla");
        cancelActivityButton.getStyleClass().add("activities-secondary-button");
        formMessage = new Label();
        formMessage.getStyleClass().add("activities-form-message");
        showDetailPlaceholder();

        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("activities-content");

        HBox titleBar = new HBox(12);
        titleBar.getStyleClass().add("activities-title-bar");
        VBox titleBox = new VBox(4);
        Label title = new Label("Attività");
        title.getStyleClass().add("activities-title");
        Label subtitle = new Label("Pianifica operazioni aziendali su gruppi di clienti e monitora lo stato cliente per cliente.");
        subtitle.getStyleClass().add("activities-subtitle");
        subtitle.setWrapText(true);
        titleBox.getChildren().addAll(title, subtitle);
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleBox, titleSpacer, newActivityButton);

        HBox workspace = new HBox(16);
        workspace.getStyleClass().add("activities-workspace");
        VBox listPanel = createListPanel();
        HBox.setHgrow(listPanel, Priority.ALWAYS);
        HBox.setHgrow(detailPanel, Priority.ALWAYS);
        workspace.getChildren().addAll(listPanel, detailPanel);
        VBox.setVgrow(workspace, Priority.ALWAYS);

        content.getChildren().addAll(titleBar, workspace);
        return content;
    }

    private VBox createListPanel() {
        VBox listPanel = new VBox(10);
        listPanel.getStyleClass().add("activities-list-panel");
        Label listTitle = new Label("Elenco attività");
        listTitle.getStyleClass().add("activities-panel-title");
        Label listSubtitle = new Label("Seleziona una riga per aprire il dettaglio e la lista clienti collegata.");
        listSubtitle.getStyleClass().add("activities-panel-subtitle");
        listSubtitle.setWrapText(true);
        VBox.setVgrow(activityScrollPane, Priority.ALWAYS);
        listPanel.getChildren().addAll(listTitle, listSubtitle, createListHeader(), activityScrollPane);
        return listPanel;
    }

    private HBox createListHeader() {
        HBox row = new HBox(8);
        row.getStyleClass().addAll("activities-row", "activities-header-row");
        row.getChildren().addAll(
                createHeaderCell("Attività", 210),
                createHeaderCell("Priorità", 70),
                createHeaderCell("Stato", 95),
                createHeaderCell("Scadenza", 90),
                createHeaderCell("Clienti", 70),
                createHeaderCell("Avanzamento", 150)
        );
        return row;
    }

    private HBox createClientHeader() {
        HBox row = new HBox(8);
        row.getStyleClass().addAll("activities-client-row", "activities-client-header-row");
        row.getChildren().addAll(
                createHeaderCell("Cliente", 190),
                createHeaderCell("Stato", 90),
                createHeaderCell("Ultimo", 80),
                createHeaderCell("Prossimo", 80),
                createHeaderCell("Nota", 220)
        );
        return row;
    }

    public void showLoading() {
        showListMessage("Caricamento attività...");
        showDetailPlaceholder();
    }

    public void showEmpty() {
        showListMessage("Nessuna attività presente. Usa \"+ Nuova attività\" per crearne una.");
        showDetailPlaceholder();
    }

    public void showError(String message) {
        showListMessage(message == null || message.isBlank() ? "Caricamento attività non riuscito." : message);
        showDetailPlaceholder();
    }

    public void clearActivities() {
        activityRows.getChildren().clear();
        activityScrollPane.setVvalue(0);
    }

    public void addActivityRow(AttivitaListRecord activity, Consumer<UUID> onSelect) {
        HBox row = new HBox(8);
        row.getStyleClass().addAll("activities-row", "activities-data-row");
        row.getChildren().addAll(
                createActivityTitleCell(activity),
                createTextCell(priorityLabel(activity.priorita()), 70),
                createBadge(activity.stato(), "activities-state-badge", 95),
                createTextCell(formatDate(activity.dataFine()), 90),
                createTextCell(String.valueOf(activity.totaleClienti()), 70),
                createProgressCell(activity)
        );
        row.setOnMouseClicked(event -> onSelect.accept(activity.id()));
        activityRows.getChildren().add(row);
    }

    public void showDetailLoading() {
        showDetailClientLayout();
        detailTitle.setText("Caricamento dettaglio...");
        detailDescription.setText("");
        detailMetaGrid.getChildren().clear();
        detailClientRows.getChildren().setAll(detailEmptyLabel);
        detailEmptyLabel.setText("Caricamento clienti attività...");
    }

    public void showDetail(AttivitaDetailRecord detail) {
        showDetailClientLayout();
        detailTitle.setText(valueOrFallback(detail.titolo(), "Attività senza titolo"));
        detailDescription.setText(valueOrFallback(detail.descrizione(), "Nessuna descrizione inserita."));
        renderDetailMeta(detail);
        renderDetailClients(detail);
    }

    public void showDetailError(String message) {
        showDetailClientLayout();
        detailTitle.setText("Dettaglio non disponibile");
        detailDescription.setText(message == null || message.isBlank() ? "Impossibile caricare il dettaglio attività." : message);
        detailMetaGrid.getChildren().clear();
        detailClientRows.getChildren().setAll(detailEmptyLabel);
        detailEmptyLabel.setText("Nessun dato da mostrare.");
    }

    public void showCreateForm() {
        clearActivityForm();
        detailTitle.setText("Nuova attività");
        detailDescription.setText("Inserisci i dati principali. La selezione clienti sarà il prossimo step guidato.");
        detailPanel.getChildren().clear();
        detailPanel.getChildren().addAll(detailTitle, detailDescription, createActivityForm());
    }

    public void showFormError(String message) {
        formMessage.setText(message == null || message.isBlank() ? "Salvataggio attività non riuscito." : message);
    }

    public void clearActivityForm() {
        titleField.clear();
        descriptionArea.clear();
        priorityChoiceBox.getSelectionModel().select(1);
        stateChoiceBox.getSelectionModel().selectFirst();
        if (!typeChoiceBox.getItems().isEmpty()) {
            typeChoiceBox.getSelectionModel().selectFirst();
        }
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        formMessage.setText("");
    }

    private VBox createActivityForm() {
        VBox form = new VBox(12);
        form.getStyleClass().add("activities-form");
        form.getChildren().addAll(
                createFormField("Titolo", titleField),
                createFormField("Descrizione", descriptionArea),
                createFormRow(
                        createFormField("Priorità", priorityChoiceBox),
                        createFormField("Stato", stateChoiceBox),
                        createFormField("Tipo attività", typeChoiceBox)
                ),
                createFormRow(
                        createFormField("Data inizio", startDatePicker),
                        createFormField("Data fine", endDatePicker)
                ),
                formMessage,
                createFormActions()
        );
        return form;
    }

    private VBox createFormField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("activities-form-label");
        box.getChildren().addAll(label, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private HBox createFormRow(VBox... fields) {
        HBox row = new HBox(10);
        row.getStyleClass().add("activities-form-row");
        row.getChildren().addAll(fields);
        return row;
    }

    private HBox createFormActions() {
        HBox actions = new HBox(10);
        actions.getStyleClass().add("activities-form-actions");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        actions.getChildren().addAll(spacer, cancelActivityButton, saveActivityButton);
        return actions;
    }

    public void showDetailPlaceholder() {
        showDetailClientLayout();
        detailTitle.setText("Seleziona un'attività");
        detailDescription.setText("Apri un'attività dalla lista per vedere clienti, stati e ultimo contatto.");
        detailMetaGrid.getChildren().clear();
        detailClientRows.getChildren().setAll(detailEmptyLabel);
        detailEmptyLabel.setText("Nessun cliente selezionato.");
    }

    private void showDetailClientLayout() {
        detailPanel.getChildren().setAll(detailTitle, detailDescription, detailMetaGrid, detailClientHeader, detailClientRows);
    }

    private void renderDetailMeta(AttivitaDetailRecord detail) {
        detailMetaGrid.getChildren().clear();
        addMeta("Tipo", valueOrFallback(detail.tipoAttivita(), "-"), 0);
        addMeta("Priorità", priorityLabel(detail.priorita()), 1);
        addMeta("Stato", stateLabel(detail.stato()), 2);
        addMeta("Inizio", formatDate(detail.dataInizio()), 3);
        addMeta("Fine", formatDate(detail.dataFine()), 4);
        addMeta("Clienti", String.valueOf(detail.clienti().size()), 5);
    }

    private void renderDetailClients(AttivitaDetailRecord detail) {
        detailClientRows.getChildren().clear();
        if (detail.clienti().isEmpty()) {
            detailEmptyLabel.setText("Nessun cliente collegato a questa attività.");
            detailClientRows.getChildren().add(detailEmptyLabel);
            return;
        }
        for (AttivitaClienteRecord cliente : detail.clienti()) {
            detailClientRows.getChildren().add(createClientRow(cliente));
        }
    }

    private HBox createClientRow(AttivitaClienteRecord cliente) {
        HBox row = new HBox(8);
        row.getStyleClass().add("activities-client-row");
        Label note = createTextCell(valueOrFallback(cliente.testoUltimaInterazione(), "-"), 220);
        note.setWrapText(true);
        row.getChildren().addAll(
                createTextCell(valueOrFallback(cliente.ragioneSociale(), "Cliente"), 190),
                createBadge(cliente.stato(), "activities-client-state-badge", 90),
                createTextCell(formatDate(cliente.dataUltimoContatto()), 80),
                createTextCell(formatDate(cliente.prossimoContatto()), 80),
                note
        );
        return row;
    }

    private Label createActivityTitleCell(AttivitaListRecord activity) {
        Label label = createTextCell(valueOrFallback(activity.titolo(), "Attività"), 210);
        label.getStyleClass().add("activities-main-cell");
        return label;
    }

    private Label createProgressCell(AttivitaListRecord activity) {
        String text = "✓ " + activity.completati()
                + " · ↻ " + activity.inCorso()
                + " · ⏸ " + activity.sospesi()
                + " · × " + activity.annullati()
                + " · … " + activity.daFare();
        return createTextCell(text, 150);
    }

    private void addMeta(String label, String value, int index) {
        VBox box = new VBox(3);
        box.getStyleClass().add("activities-meta-card");
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("activities-meta-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("activities-meta-value");
        valueNode.setWrapText(true);
        box.getChildren().addAll(labelNode, valueNode);
        detailMetaGrid.add(box, index % 3, index / 3);
    }

    private void showListMessage(String message) {
        activityRows.getChildren().clear();
        HBox row = new HBox();
        row.getStyleClass().add("activities-empty-row");
        Label label = new Label(message);
        label.getStyleClass().add("activities-empty-label");
        label.setWrapText(true);
        row.getChildren().add(label);
        activityRows.getChildren().add(row);
        activityScrollPane.setVvalue(0);
    }

    private Label createHeaderCell(String text, double width) {
        Label label = createTextCell(text, width);
        label.getStyleClass().add("activities-header-cell");
        return label;
    }

    private Label createTextCell(String text, double width) {
        Label label = new Label(valueOrFallback(text, "-"));
        label.getStyleClass().add("activities-table-cell");
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setMaxWidth(width);
        return label;
    }

    private Label createBadge(String value, String styleClass, double width) {
        Label label = createTextCell(stateLabel(value), width);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private String priorityLabel(Integer priority) {
        if (priority == null) {
            return "Normale";
        }
        return switch (priority) {
            case 1 -> "Bassa";
            case 3 -> "Alta";
            default -> "Normale";
        };
    }

    private String stateLabel(String state) {
        if (state == null || state.isBlank()) {
            return "-";
        }
        return switch (state) {
            case "BOZZA" -> "Bozza";
            case "IN_CORSO" -> "In corso";
            case "SOSPESA" -> "Sospesa";
            case "COMPLETATA" -> "Completata";
            case "ANNULLATA" -> "Annullata";
            case "DA_FARE" -> "Da fare";
            case "SOSPESO" -> "Sospeso";
            case "COMPLETATO" -> "Completato";
            case "ANNULLATO" -> "Annullato";
            default -> state.replace('_', ' ');
        };
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getNewActivityButton() {
        return newActivityButton;
    }

    public Button getSaveActivityButton() {
        return saveActivityButton;
    }

    public Button getCancelActivityButton() {
        return cancelActivityButton;
    }

    public String getActivityTitle() {
        return titleField.getText();
    }

    public String getActivityDescription() {
        return descriptionArea.getText();
    }

    public Integer getSelectedPriority() {
        PriorityOption option = priorityChoiceBox.getSelectionModel().getSelectedItem();
        return option == null ? null : option.value();
    }

    public String getSelectedState() {
        StateOption option = stateChoiceBox.getSelectionModel().getSelectedItem();
        return option == null ? null : option.value();
    }

    public UUID getSelectedTypeId() {
        TipoAttivitaOption option = typeChoiceBox.getSelectionModel().getSelectedItem();
        return option == null ? null : option.id();
    }

    public LocalDate getStartDate() {
        return startDatePicker.getValue();
    }

    public LocalDate getEndDate() {
        return endDatePicker.getValue();
    }

    public void setTipoAttivitaOptions(List<TipoAttivitaOption> options) {
        typeChoiceBox.getItems().setAll(TipoAttivitaOption.empty());
        typeChoiceBox.getItems().addAll(options);
        typeChoiceBox.getSelectionModel().selectFirst();
    }

    public record PriorityOption(Integer value, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record StateOption(String value, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record TipoAttivitaOption(UUID id, String label) {
        public static TipoAttivitaOption empty() {
            return new TipoAttivitaOption(null, "Nessun tipo");
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
