package com.example.clients.feature.clienti.schedacliente.controller;

import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ClienteProfile;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.TimelineFilter;
import com.example.clients.feature.clienti.schedacliente.view.SchedaClienteView;

import java.util.UUID;
import javafx.scene.control.Alert;

public class SchedaClienteController {

    private final SchedaClienteView view;
    private final ClientiNav clientiNav;
    private final SchedaClienteService service;
    private EditorMode editorMode = EditorMode.NOTE;

    public SchedaClienteController(SchedaClienteView view, ClientiNav clientiNav, SchedaClienteService service, UUID clienteId) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
        try {
            render(service.loadProfile(clienteId));
        } catch (RuntimeException e) {
            showError("Caricamento scheda non riuscito", e);
        }
    }

    private void configureActions() {
        view.getFavoriteButton().setOnAction(event -> runAndRender("Aggiornamento preferito non riuscito", service::toggleFavorite));
        view.getEditProfileButton().setOnAction(event -> openProfileEditor());
        view.getCancelProfileEditButton().setOnAction(event -> runAndRender("Annullamento modifica non riuscito", service::cancelEdit));
        view.getSaveProfileEditButton().setOnAction(event -> runAndRender("Salvataggio scheda non riuscito", () -> service.saveEdit(view.collectEditDraft())));
        view.getNewNoteButton().setOnAction(event -> openNoteEditor());
        view.getNewCallButton().setOnAction(event -> openCallEditor());
        view.getAllFilterButton().setOnAction(event -> applyTimelineFilter(TimelineFilter.ALL));
        view.getNotesFilterButton().setOnAction(event -> applyTimelineFilter(TimelineFilter.NOTES));
        view.getCallsFilterButton().setOnAction(event -> applyTimelineFilter(TimelineFilter.CALLS));
        view.getCancelNoteButton().setOnAction(event -> view.hideNoteEditor());
        view.getSaveNoteButton().setOnAction(event -> saveEditorContent());
    }

    private void openProfileEditor() {
        view.hideNoteEditor();
        applyTimelineFilter(TimelineFilter.ALL);
        view.renderEditableProfile(service.startEdit());
    }

    private void openNoteEditor() {
        editorMode = EditorMode.NOTE;
        view.showNoteEditor();
    }

    private void openCallEditor() {
        editorMode = EditorMode.CALL;
        view.showCallEditor();
    }

    private void applyTimelineFilter(TimelineFilter filter) {
        view.setActiveTimelineFilter(filter);
        render(service.setTimelineFilter(filter));
    }

    private void saveEditorContent() {
        runAndRender("Salvataggio interazione non riuscito", () -> {
            if (editorMode == EditorMode.CALL) {
                return service.addChiamata(view.getNoteTextArea().getText(), view.getNextCallDatePicker().getValue());
            }
            return service.addNota(view.getNoteTextArea().getText());
        });
        view.hideNoteEditor();
    }

    private void runAndRender(String errorTitle, ProfileAction action) {
        try {
            render(action.run());
        } catch (RuntimeException e) {
            showError(errorTitle, e);
        }
    }

    private void showError(String title, RuntimeException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage() == null ? "Errore imprevisto." : e.getMessage());
        alert.showAndWait();
    }

    private void render(ClienteProfile profile) {
        view.renderProfile(profile);
    }

    public SchedaClienteView getView() {
        return view;
    }

    public ClientiNav getClientiNav() {
        return clientiNav;
    }

    public SchedaClienteService getService() {
        return service;
    }

    @FunctionalInterface
    private interface ProfileAction {
        ClienteProfile run();
    }

    private enum EditorMode {
        NOTE,
        CALL
    }
}
