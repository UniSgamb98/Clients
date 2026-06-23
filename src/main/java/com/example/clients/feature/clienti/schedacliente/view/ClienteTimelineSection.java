package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.TimelineFilter;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

final class ClienteTimelineSection extends VBox {

    private final Button newNoteButton;
    private final Button newCallButton;
    private final Button allFilterButton;
    private final Button notesFilterButton;
    private final Button callsFilterButton;
    private final VBox timelineList;
    private final VBox noteEditor;
    private final DatePicker nextCallDatePicker;
    private final TextArea noteTextArea;
    private final Button saveNoteButton;
    private final Button cancelNoteButton;

    ClienteTimelineSection() {
        super(12);
        getStyleClass().add("new-client-section");
        Label title = new Label("Interazioni");
        title.getStyleClass().add("new-client-section-title");

        newNoteButton = new Button("+ Nuova nota");
        newNoteButton.getStyleClass().add("clients-primary-button");
        newCallButton = new Button("+ Nuova chiamata");
        newCallButton.getStyleClass().add("clients-filter-button");
        allFilterButton = createTimelineFilterButton("Tutti");
        notesFilterButton = createTimelineFilterButton("Solo note");
        callsFilterButton = createTimelineFilterButton("Solo chiamate");
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

        getChildren().addAll(title, createActions(), createFilters(), noteEditor, timelineList);
    }

    VBox getTimelineList() {
        return timelineList;
    }

    void setEditMode(boolean editMode) {
        newNoteButton.setDisable(editMode);
        newCallButton.setDisable(editMode);
        allFilterButton.setDisable(editMode);
        notesFilterButton.setDisable(editMode);
        callsFilterButton.setDisable(editMode);
    }

    void showNoteEditor() {
        saveNoteButton.setText("Salva nota");
        nextCallDatePicker.setVisible(false);
        nextCallDatePicker.setManaged(false);
        showEditor();
    }

    void showCallEditor() {
        saveNoteButton.setText("Salva chiamata");
        nextCallDatePicker.setVisible(true);
        nextCallDatePicker.setManaged(true);
        showEditor();
    }

    void hideNoteEditor() {
        noteEditor.setVisible(false);
        noteEditor.setManaged(false);
        noteTextArea.clear();
        nextCallDatePicker.setValue(null);
    }

    void setActiveTimelineFilter(TimelineFilter filter) {
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

    Button getNewNoteButton() {
        return newNoteButton;
    }

    Button getNewCallButton() {
        return newCallButton;
    }

    Button getAllFilterButton() {
        return allFilterButton;
    }

    Button getNotesFilterButton() {
        return notesFilterButton;
    }

    Button getCallsFilterButton() {
        return callsFilterButton;
    }

    DatePicker getNextCallDatePicker() {
        return nextCallDatePicker;
    }

    TextArea getNoteTextArea() {
        return noteTextArea;
    }

    Button getSaveNoteButton() {
        return saveNoteButton;
    }

    Button getCancelNoteButton() {
        return cancelNoteButton;
    }

    private void showEditor() {
        noteEditor.setVisible(true);
        noteEditor.setManaged(true);
        noteTextArea.requestFocus();
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

    private HBox createActions() {
        HBox actions = new HBox(10);
        actions.getStyleClass().add("client-profile-timeline-actions");
        actions.getChildren().addAll(newNoteButton, newCallButton);
        return actions;
    }

    private HBox createFilters() {
        HBox filters = new HBox(6);
        filters.getStyleClass().add("client-profile-timeline-filter-bar");
        filters.getChildren().addAll(allFilterButton, notesFilterButton, callsFilterButton);
        return filters;
    }

    private Button createTimelineFilterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("client-profile-small-filter-button");
        return button;
    }
}
