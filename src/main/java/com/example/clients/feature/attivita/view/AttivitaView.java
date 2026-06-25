package com.example.clients.feature.attivita.view;

import com.example.clients.core.database.query.AttivitaQuery.AttivitaClienteRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaDetailRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaListRecord;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        detailPanel.getChildren().addAll(detailTitle, detailDescription, detailMetaGrid, createClientHeader(), detailClientRows);
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
        detailTitle.setText("Caricamento dettaglio...");
        detailDescription.setText("");
        detailMetaGrid.getChildren().clear();
        detailClientRows.getChildren().setAll(detailEmptyLabel);
        detailEmptyLabel.setText("Caricamento clienti attività...");
    }

    public void showDetail(AttivitaDetailRecord detail) {
        detailTitle.setText(valueOrFallback(detail.titolo(), "Attività senza titolo"));
        detailDescription.setText(valueOrFallback(detail.descrizione(), "Nessuna descrizione inserita."));
        renderDetailMeta(detail);
        renderDetailClients(detail);
    }

    public void showDetailError(String message) {
        detailTitle.setText("Dettaglio non disponibile");
        detailDescription.setText(message == null || message.isBlank() ? "Impossibile caricare il dettaglio attività." : message);
        detailMetaGrid.getChildren().clear();
        detailClientRows.getChildren().setAll(detailEmptyLabel);
        detailEmptyLabel.setText("Nessun dato da mostrare.");
    }

    private void showDetailPlaceholder() {
        detailTitle.setText("Seleziona un'attività");
        detailDescription.setText("Apri un'attività dalla lista per vedere clienti, stati e ultimo contatto.");
        detailMetaGrid.getChildren().clear();
        detailClientRows.getChildren().setAll(detailEmptyLabel);
        detailEmptyLabel.setText("Nessun cliente selezionato.");
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
}
