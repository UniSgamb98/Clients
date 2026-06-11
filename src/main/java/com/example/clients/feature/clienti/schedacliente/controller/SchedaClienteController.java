package com.example.clients.feature.clienti.schedacliente.controller;

import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ClienteProfile;
import com.example.clients.feature.clienti.schedacliente.view.SchedaClienteView;

public class SchedaClienteController {

    private final SchedaClienteView view;
    private final ClientiNav clientiNav;
    private final SchedaClienteService service;

    public SchedaClienteController(SchedaClienteView view, ClientiNav clientiNav, SchedaClienteService service, String clienteName) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
        render(service.loadProfile(clienteName));
    }

    private void configureActions() {
        view.getFavoriteButton().setOnAction(event -> render(service.toggleFavorite()));
        view.getNewAnnotationButton().setOnAction(event -> view.showNoteEditor());
        view.getCancelNoteButton().setOnAction(event -> view.hideNoteEditor());
        view.getSaveNoteButton().setOnAction(event -> {
            render(service.addAnnotazione(view.getNoteTextArea().getText()));
            view.hideNoteEditor();
        });
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
}
