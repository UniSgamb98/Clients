package com.example.clients.feature.clienti.schedacliente.controller;

import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ClienteProfile;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.TimelineFilter;
import com.example.clients.feature.clienti.schedacliente.view.SchedaClienteView;

public class SchedaClienteController {

    private final SchedaClienteView view;
    private final ClientiNav clientiNav;
    private final SchedaClienteService service;
    private EditorMode editorMode = EditorMode.NOTE;

    public SchedaClienteController(SchedaClienteView view, ClientiNav clientiNav, SchedaClienteService service, String clienteName) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
        render(service.loadProfile(clienteName));
    }

    private void configureActions() {
        view.getFavoriteButton().setOnAction(event -> render(service.toggleFavorite()));
        view.getEditProfileButton().setOnAction(event -> openProfileEditor());
        view.getCancelProfileEditButton().setOnAction(event -> render(service.cancelEdit()));
        view.getSaveProfileEditButton().setOnAction(event -> render(service.saveEdit(view.collectEditDraft())));
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
        if (editorMode == EditorMode.CALL) {
            render(service.addChiamata(view.getNoteTextArea().getText(), view.getNextCallDatePicker().getValue()));
        } else {
            render(service.addNota(view.getNoteTextArea().getText()));
        }
        view.hideNoteEditor();
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

    private enum EditorMode {
        NOTE,
        CALL
    }
}
