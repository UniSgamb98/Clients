package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ClienteProfile;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.EditProfileDraft;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionPreview;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionType;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.TimelineFilter;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SchedaClienteView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double SIDE_COLUMN_MIN_WIDTH = 330;
    private static final double SIDE_COLUMN_PREF_WIDTH = 560;
    private static final double SIDE_COLUMN_MAX_WIDTH = 620;
    private static final double RELATED_SECTIONS_GAP = 12;
    private static final double RELATED_SECTIONS_TWO_COLUMN_BREAKPOINT = 560;

    private final AppSidebar sidebar;
    private final ClienteProfileHeader header;
    private final ClienteDataSection dataSection;
    private final ClienteRelatedSections relatedSections;
    private final ClienteProfileDetailsPanel detailsPanel;
    private final ClienteTimelineSection timelineSection;

    public SchedaClienteView() {
        sidebar = new AppSidebar();
        header = new ClienteProfileHeader();
        dataSection = new ClienteDataSection();
        relatedSections = new ClienteRelatedSections(RELATED_SECTIONS_GAP, RELATED_SECTIONS_TWO_COLUMN_BREAKPOINT);
        detailsPanel = new ClienteProfileDetailsPanel(dataSection, relatedSections);
        timelineSection = new ClienteTimelineSection();
        dataSection.setLinkedOptionsRefresh(relatedSections::refreshLinkedContactOptions);
        setEditMode(false);

        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("clients-content");

        VBox body = new VBox(18);
        body.getChildren().addAll(header, createMainColumns());

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("new-client-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        content.getChildren().add(scrollPane);
        return content;
    }

    private HBox createMainColumns() {
        HBox columns = new HBox(18);
        VBox leftColumn = new VBox(14);
        VBox rightColumn = new VBox(14);
        leftColumn.getStyleClass().addAll("client-profile-column", "client-profile-side-column");
        rightColumn.getStyleClass().addAll("client-profile-column", "client-profile-main-column");
        leftColumn.setMinWidth(SIDE_COLUMN_MIN_WIDTH);
        leftColumn.setPrefWidth(SIDE_COLUMN_PREF_WIDTH);
        leftColumn.setMaxWidth(SIDE_COLUMN_MAX_WIDTH);
        rightColumn.setMaxWidth(Double.MAX_VALUE);
        leftColumn.getChildren().add(detailsPanel);
        rightColumn.getChildren().add(timelineSection);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        columns.getChildren().addAll(leftColumn, rightColumn);
        return columns;
    }

    public void renderProfile(ClienteProfile profile) {
        setEditMode(false);
        header.setTitle(profile.ragioneSociale());
        header.setSubtitle(profile.tipoCliente() + " · " + profile.statoTrattativa());
        header.setAcquisitionText("Acquisito " + formatDate(profile.acquisizione()));
        header.setLastInteractionText("Ultima chiamata " + lastCallText(profile.interazioni()));
        header.setNextInteractionText("Prossima chiamata " + nextCallText(profile.interazioni()));
        setFavorite(profile.favorite());
        setInvolvementSliderValue(profile.coinvolgimento());
        dataSection.render(profile);
        relatedSections.renderContacts(profile.contatti());
        relatedSections.renderAddresses(profile.indirizzi());
        timelineSection.render(profile.interazioni());
    }

    public void renderEditableProfile(EditProfileDraft draft) {
        setEditMode(true);
        header.setTitle(draft.ragioneSociale().isBlank() ? "Cliente" : draft.ragioneSociale());
        header.setSubtitle("Modifica dati cliente");
        header.setAcquisitionText("Acquisito " + formatDate(draft.acquisizione()));
        header.setLastInteractionText("Ultima chiamata " + lastEditableCallText(draft.interazioni()));
        header.setNextInteractionText("Prossima chiamata " + nextEditableCallText(draft.interazioni()));
        setActiveTimelineFilter(TimelineFilter.ALL);
        dataSection.renderEditor(draft);
        relatedSections.setCompanyValueSources(dataSection.phoneEditFields(), dataSection.emailEditFields());
        relatedSections.renderContactsEditor(draft.contatti());
        relatedSections.renderAddressesEditor(draft.indirizzi());
        timelineSection.renderEditor(draft.interazioni());
    }

    public EditProfileDraft collectEditDraft() {
        return new EditProfileDraft(
                dataSection.ragioneSociale(),
                dataSection.tipoCliente(),
                dataSection.statoTrattativa(),
                dataSection.coinvolgimento(),
                dataSection.partitaIva(),
                dataSection.codiceFiscale(),
                dataSection.acquisizione(),
                dataSection.telefoni(),
                dataSection.email(),
                dataSection.sitiWeb(),
                relatedSections.collectAddresses(),
                relatedSections.collectContacts(),
                timelineSection.collectInteractions()
        );
    }

    private void setEditMode(boolean editMode) {
        header.setEditMode(editMode);
        timelineSection.setEditMode(editMode);
    }

    public void setFavorite(boolean favorite) {
        header.setFavorite(favorite);
    }

    public void showNoteEditor() {
        timelineSection.showNoteEditor();
    }

    public void showCallEditor() {
        timelineSection.showCallEditor();
    }

    public void hideNoteEditor() {
        timelineSection.hideNoteEditor();
    }

    public void setActiveTimelineFilter(TimelineFilter filter) {
        timelineSection.setActiveTimelineFilter(filter);
    }

    private String lastCallText(List<InteractionPreview> interactions) {
        return interactions.stream()
                .filter(interaction -> interaction.type() == InteractionType.CHIAMATA)
                .findFirst()
                .map(interaction -> DATE_FORMATTER.format(interaction.data()))
                .orElse("-");
    }

    private String nextCallText(List<InteractionPreview> interactions) {
        return interactions.stream()
                .map(InteractionPreview::prossimoContatto)
                .filter(nextContact -> nextContact != null)
                .findFirst()
                .map(DATE_FORMATTER::format)
                .orElse("-");
    }

    private String lastEditableCallText(List<InteractionEditInput> interactions) {
        return interactions.stream()
                .filter(interaction -> interaction.type() == InteractionType.CHIAMATA)
                .findFirst()
                .map(interaction -> DATE_FORMATTER.format(interaction.data()))
                .orElse("-");
    }

    private String nextEditableCallText(List<InteractionEditInput> interactions) {
        return interactions.stream()
                .map(InteractionEditInput::prossimoContatto)
                .filter(nextContact -> nextContact != null)
                .findFirst()
                .map(DATE_FORMATTER::format)
                .orElse("-");
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    public void setTipoClienteOptions(List<String> options) {
        dataSection.setTipoClienteOptions(options);
    }

    public void setStatoTrattativaOptions(List<String> options) {
        dataSection.setStatoTrattativaOptions(options);
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Slider getInvolvementSlider() {
        return header.getInvolvementSlider();
    }

    public boolean isUpdatingInvolvementSlider() {
        return header.isUpdatingInvolvementSlider();
    }

    public int involvementSliderValue() {
        return header.involvementSliderValue();
    }

    public void setInvolvementSliderValue(Integer value) {
        header.setInvolvementSliderValue(value);
    }

    public Button getFavoriteButton() {
        return header.getFavoriteButton();
    }

    public Button getEditProfileButton() {
        return header.getEditProfileButton();
    }

    public Button getSaveProfileEditButton() {
        return header.getSaveProfileEditButton();
    }

    public Button getCancelProfileEditButton() {
        return header.getCancelProfileEditButton();
    }

    public Button getNewNoteButton() {
        return timelineSection.getNewNoteButton();
    }

    public Button getNewCallButton() {
        return timelineSection.getNewCallButton();
    }

    public Button getAllFilterButton() {
        return timelineSection.getAllFilterButton();
    }

    public Button getNotesFilterButton() {
        return timelineSection.getNotesFilterButton();
    }

    public Button getCallsFilterButton() {
        return timelineSection.getCallsFilterButton();
    }

    public DatePicker getNextCallDatePicker() {
        return timelineSection.getNextCallDatePicker();
    }

    public TextArea getNoteTextArea() {
        return timelineSection.getNoteTextArea();
    }

    public Button getSaveNoteButton() {
        return timelineSection.getSaveNoteButton();
    }

    public Button getCancelNoteButton() {
        return timelineSection.getCancelNoteButton();
    }
}
