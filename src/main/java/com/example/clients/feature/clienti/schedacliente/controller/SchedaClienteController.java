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
        view.getInvolvementSlider().valueProperty().addListener((observable, oldValue, newValue) -> updateCoinvolgimentoFromSlider());
        view.getInvolvementSlider().valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
            if (!isChanging) {
                updateCoinvolgimentoFromSlider();
            }
        });
        view.getEditProfileButton().setOnAction(event -> openProfileEditor());
        view.getCancelProfileEditButton().setOnAction(event -> runAndRender("Annullamento modifica non riuscito", service::cancelEdit));
        view.getSaveProfileEditButton().setOnAction(event -> runAndRender("Salvataggio scheda non riuscito", () -> service.saveEdit(view.collectEditDraft())));
        view.getEditForniButton().setOnAction(event -> openForniEditor());
        view.getCancelForniButton().setOnAction(event -> closeForniEditor());
        view.getSaveForniButton().setOnAction(event -> saveForniEditor());
        view.getNewNoteButton().setOnAction(event -> openNoteEditor());
        view.getNewCallButton().setOnAction(event -> openCallEditor());
        view.getAllFilterButton().setOnAction(event -> applyTimelineFilter(TimelineFilter.ALL));
        view.getNotesFilterButton().setOnAction(event -> applyTimelineFilter(TimelineFilter.NOTES));
        view.getCallsFilterButton().setOnAction(event -> applyTimelineFilter(TimelineFilter.CALLS));
        view.getCancelNoteButton().setOnAction(event -> view.hideNoteEditor());
        view.getSaveNoteButton().setOnAction(event -> saveEditorContent());
    }

    private void updateCoinvolgimentoFromSlider() {
        if (view.isUpdatingInvolvementSlider() || view.getInvolvementSlider().isValueChanging()) {
            return;
        }

        runAndRender(
                "Aggiornamento coinvolgimento non riuscito",
                () -> service.updateCoinvolgimento(view.involvementSliderValue())
        );
    }

    private void openProfileEditor() {
        view.hideNoteEditor();
        applyTimelineFilter(TimelineFilter.ALL);
        view.setTipoClienteOptions(service.getTipiCliente());
        view.setStatoTrattativaOptions(service.getStatiTrattativa());
        view.setForniCatalog(service.getForniCatalog());
        view.setFresatoriCatalog(service.getFresatoriCatalog());
        view.renderEditableProfile(service.startEdit());
    }

    private void openForniEditor() {
        try {
            view.setForniCatalog(service.getForniCatalog());
            view.renderStandaloneForniEditor(service.startForniEdit());
            view.getEditProfileButton().setDisable(true);
        } catch (RuntimeException e) {
            showError("Apertura modifica forni non riuscita", e);
        }
    }

    private void closeForniEditor() {
        try {
            view.renderForni(service.cancelForniEdit());
            view.getEditProfileButton().setDisable(false);
        } catch (RuntimeException e) {
            showError("Annullamento modifica forni non riuscito", e);
        }
    }

    private void saveForniEditor() {
        try {
            view.renderForni(service.saveForniEdit(view.collectForni()));
            view.getEditProfileButton().setDisable(false);
        } catch (RuntimeException e) {
            showError("Salvataggio forni non riuscito", e);
        }
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
                return service.addChiamata(callText(), view.getNextCallDatePicker().getValue());
            }
            return service.addNota(view.getNoteTextArea().getText());
        });
        view.hideNoteEditor();
    }

    private String callText() {
        String text = view.getNoteTextArea().getText() == null ? "" : view.getNoteTextArea().getText().trim();
        String outcome = selectedChoiceValue(view.getCallOutcomeChoiceBox().getValue(), "Esito chiamata");
        String priority = selectedChoiceValue(view.getCallPriorityChoiceBox().getValue(), "Priorità");
        String details = java.util.stream.Stream.of(
                        outcome == null ? null : "Esito: " + outcome,
                        priority == null ? null : "Priorità: " + priority)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining("\n"));
        if (details.isBlank()) {
            return text;
        }
        if (text.isBlank()) {
            return details;
        }
        return text + "\n" + details;
    }

    private String selectedChoiceValue(String value, String placeholder) {
        return value == null || value.equals(placeholder) ? null : value;
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
