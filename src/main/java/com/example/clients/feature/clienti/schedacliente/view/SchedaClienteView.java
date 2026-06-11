package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ClienteProfile;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionPreview;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.TimelineFilter;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SchedaClienteView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final Label vatLabel;
    private final Label fiscalCodeLabel;
    private final Label acquisitionLabel;
    private final Label lastInteractionLabel;
    private final Label nextInteractionLabel;
    private final Button favoriteButton;
    private final Button newNoteButton;
    private final Button newCallButton;
    private final Button allFilterButton;
    private final Button notesFilterButton;
    private final Button callsFilterButton;
    private final VBox customerDataList;
    private final VBox contactsList;
    private final VBox addressesList;
    private final VBox timelineList;
    private final VBox noteEditor;
    private final DatePicker nextCallDatePicker;
    private final TextArea noteTextArea;
    private final Button saveNoteButton;
    private final Button cancelNoteButton;

    public SchedaClienteView() {
        header = new AppHeader("Scheda cliente");
        sidebar = new AppSidebar();
        titleLabel = new Label("Cliente");
        titleLabel.getStyleClass().add("clients-title");
        subtitleLabel = new Label("Profilo cliente e storico comunicazioni");
        subtitleLabel.getStyleClass().add("clients-subtitle");
        vatLabel = createBadgeLabel();
        fiscalCodeLabel = createBadgeLabel();
        acquisitionLabel = createBadgeLabel();
        lastInteractionLabel = createBadgeLabel();
        nextInteractionLabel = createBadgeLabel();
        favoriteButton = new Button("☆");
        favoriteButton.getStyleClass().add("client-profile-favorite-button");
        newNoteButton = new Button("+ Nuova nota");
        newNoteButton.getStyleClass().add("clients-primary-button");
        newCallButton = new Button("+ Nuova chiamata");
        newCallButton.getStyleClass().add("clients-filter-button");
        allFilterButton = createTimelineFilterButton("Tutti");
        notesFilterButton = createTimelineFilterButton("Solo note");
        callsFilterButton = createTimelineFilterButton("Solo chiamate");
        customerDataList = new VBox(8);
        contactsList = new VBox(8);
        addressesList = new VBox(8);
        timelineList = new VBox(10);
        noteEditor = createNoteEditor();
        nextCallDatePicker = new DatePicker();
        nextCallDatePicker.setPromptText("Prossima chiamata");
        nextCallDatePicker.getStyleClass().add("client-profile-call-date-picker");
        noteTextArea = new TextArea();
        noteTextArea.setPromptText("Scrivi una nota sulla comunicazione con il cliente...");
        noteTextArea.getStyleClass().add("client-profile-note-area");
        saveNoteButton = new Button("Salva");
        saveNoteButton.getStyleClass().add("clients-primary-button");
        cancelNoteButton = new Button("Annulla");
        cancelNoteButton.getStyleClass().add("clients-filter-button");
        noteEditor.getChildren().addAll(nextCallDatePicker, noteTextArea, createNoteActions());
        setActiveTimelineFilter(TimelineFilter.ALL);
        hideNoteEditor();

        setTop(header);
        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("clients-content");

        VBox body = new VBox(18);
        body.getChildren().addAll(createHero(), createMainColumns());

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("new-client-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        content.getChildren().add(scrollPane);
        return content;
    }

    private VBox createHero() {
        VBox hero = new VBox(12);
        hero.getStyleClass().add("client-profile-hero");

        HBox titleRow = new HBox(12);
        titleRow.getStyleClass().add("clients-title-bar");
        VBox titleBox = new VBox(4);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleBox, spacer, favoriteButton);

        HBox badges = new HBox(10);
        badges.getStyleClass().add("client-profile-badges");
        badges.getChildren().addAll(vatLabel, fiscalCodeLabel, acquisitionLabel, lastInteractionLabel, nextInteractionLabel);

        hero.getChildren().addAll(titleRow, badges);
        return hero;
    }

    private HBox createMainColumns() {
        HBox columns = new HBox(18);
        VBox leftColumn = new VBox(14);
        VBox rightColumn = new VBox(14);
        leftColumn.getStyleClass().add("client-profile-column");
        rightColumn.getStyleClass().add("client-profile-column");
        leftColumn.getChildren().addAll(
                createSection("Dati cliente", customerDataList),
                createSection("Contatti", contactsList),
                createSection("Indirizzi", addressesList)
        );
        rightColumn.getChildren().add(createTimelineSection());
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        columns.getChildren().addAll(leftColumn, rightColumn);
        return columns;
    }

    private VBox createTimelineSection() {
        VBox section = createSection("Interazioni", timelineList);
        HBox actions = new HBox(10);
        actions.getStyleClass().add("client-profile-timeline-actions");
        actions.getChildren().addAll(newNoteButton, newCallButton);

        HBox filters = new HBox(6);
        filters.getStyleClass().add("client-profile-timeline-filter-bar");
        filters.getChildren().addAll(allFilterButton, notesFilterButton, callsFilterButton);

        section.getChildren().add(1, actions);
        section.getChildren().add(2, filters);
        section.getChildren().add(3, noteEditor);
        return section;
    }

    private VBox createSection(String titleText, VBox body) {
        VBox section = new VBox(12);
        section.getStyleClass().add("new-client-section");
        Label title = new Label(titleText);
        title.getStyleClass().add("new-client-section-title");
        section.getChildren().addAll(title, body);
        return section;
    }

    private VBox createNoteEditor() {
        VBox editor = new VBox(10);
        editor.getStyleClass().add("client-profile-note-editor");
        return editor;
    }

    private HBox createNoteActions() {
        HBox actions = new HBox(10);
        actions.getChildren().addAll(saveNoteButton, cancelNoteButton);
        return actions;
    }

    private Button createTimelineFilterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("client-profile-small-filter-button");
        return button;
    }

    private Label createBadgeLabel() {
        Label label = new Label();
        label.getStyleClass().add("client-profile-badge");
        return label;
    }

    public void renderProfile(ClienteProfile profile) {
        titleLabel.setText(profile.ragioneSociale());
        subtitleLabel.setText(profile.tipoCliente() + " · " + profile.statoTrattativa());
        vatLabel.setText("P.IVA " + emptyFallback(profile.partitaIva()));
        fiscalCodeLabel.setText("CF " + emptyFallback(profile.codiceFiscale()));
        acquisitionLabel.setText("Acquisizione " + (profile.acquisizione() == null ? "-" : DATE_FORMATTER.format(profile.acquisizione())));
        lastInteractionLabel.setText("Ultima interazione " + lastInteractionText(profile.interazioni()));
        nextInteractionLabel.setText("Prossimo contatto " + nextInteractionText(profile.interazioni()));
        setFavorite(profile.favorite());
        renderList(customerDataList, List.of(
                "Ragione sociale: " + emptyFallback(profile.ragioneSociale()),
                "Tipo cliente: " + emptyFallback(profile.tipoCliente()),
                "Stato trattativa: " + emptyFallback(profile.statoTrattativa())
        ));
        renderList(contactsList, profile.contatti());
        renderList(addressesList, profile.indirizzi());
        renderTimeline(profile.interazioni());
    }

    public void setFavorite(boolean favorite) {
        favoriteButton.setText(favorite ? "★" : "☆");
        favoriteButton.getStyleClass().remove("client-profile-favorite-active");
        if (favorite) {
            favoriteButton.getStyleClass().add("client-profile-favorite-active");
        }
    }

    public void showNoteEditor() {
        saveNoteButton.setText("Salva nota");
        nextCallDatePicker.setVisible(false);
        nextCallDatePicker.setManaged(false);
        showEditor();
    }

    public void showCallEditor() {
        saveNoteButton.setText("Salva chiamata");
        nextCallDatePicker.setVisible(true);
        nextCallDatePicker.setManaged(true);
        showEditor();
    }

    private void showEditor() {
        noteEditor.setVisible(true);
        noteEditor.setManaged(true);
        noteTextArea.requestFocus();
    }

    public void hideNoteEditor() {
        noteEditor.setVisible(false);
        noteEditor.setManaged(false);
        noteTextArea.clear();
        nextCallDatePicker.setValue(null);
    }

    public void setActiveTimelineFilter(TimelineFilter filter) {
        allFilterButton.getStyleClass().remove("client-profile-small-filter-active");
        notesFilterButton.getStyleClass().remove("client-profile-small-filter-active");
        callsFilterButton.getStyleClass().remove("client-profile-small-filter-active");

        Button activeButton = switch (filter) {
            case NOTES -> notesFilterButton;
            case CALLS -> callsFilterButton;
            case ALL -> allFilterButton;
        };
        activeButton.getStyleClass().add("client-profile-small-filter-active");
    }

    private void renderList(VBox container, List<String> values) {
        container.getChildren().clear();
        if (values.isEmpty()) {
            container.getChildren().add(createInfoLabel("Nessun dato disponibile"));
            return;
        }
        values.forEach(value -> container.getChildren().add(createInfoLabel(value)));
    }

    private void renderTimeline(List<InteractionPreview> interactions) {
        timelineList.getChildren().clear();
        if (interactions.isEmpty()) {
            timelineList.getChildren().add(createInfoLabel("Nessuna interazione registrata"));
            return;
        }
        for (InteractionPreview interaction : interactions) {
            VBox card = new VBox(4);
            card.getStyleClass().add("client-profile-timeline-card");
            Label title = createInfoLabel(DATE_FORMATTER.format(interaction.data()) + " · " + interaction.type().label());
            title.getStyleClass().add("client-profile-timeline-title");
            Label text = createInfoLabel(timelineText(interaction));
            card.getChildren().addAll(title, text);
            timelineList.getChildren().add(card);
        }
    }

    private String timelineText(InteractionPreview interaction) {
        if (interaction.prossimoContatto() == null) {
            return interaction.testo();
        }
        return interaction.testo() + "\nProssima chiamata: " + DATE_FORMATTER.format(interaction.prossimoContatto());
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("client-profile-info-label");
        label.setWrapText(true);
        return label;
    }

    private String lastInteractionText(List<InteractionPreview> interactions) {
        if (interactions.isEmpty()) {
            return "-";
        }
        return DATE_FORMATTER.format(interactions.get(0).data());
    }

    private String nextInteractionText(List<InteractionPreview> interactions) {
        return interactions.stream()
                .map(InteractionPreview::prossimoContatto)
                .filter(nextContact -> nextContact != null)
                .findFirst()
                .map(DATE_FORMATTER::format)
                .orElse("-");
    }

    private String emptyFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public AppHeader getHeader() {
        return header;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getFavoriteButton() {
        return favoriteButton;
    }

    public Button getNewNoteButton() {
        return newNoteButton;
    }

    public Button getNewCallButton() {
        return newCallButton;
    }

    public Button getAllFilterButton() {
        return allFilterButton;
    }

    public Button getNotesFilterButton() {
        return notesFilterButton;
    }

    public Button getCallsFilterButton() {
        return callsFilterButton;
    }

    public DatePicker getNextCallDatePicker() {
        return nextCallDatePicker;
    }

    public TextArea getNoteTextArea() {
        return noteTextArea;
    }

    public Button getSaveNoteButton() {
        return saveNoteButton;
    }

    public Button getCancelNoteButton() {
        return cancelNoteButton;
    }
}
