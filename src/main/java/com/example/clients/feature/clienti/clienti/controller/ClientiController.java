package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientePreview;
import com.example.clients.feature.clienti.clienti.view.ClientiView;

public class ClientiController {

    private final ClientiView view;
    private final ClientiNav clientiNav;
    private final ClientiService service;

    public ClientiController(ClientiView view, ClientiNav clientiNav, ClientiService service) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
        loadPreviewClients();
    }

    private void configureActions() {
        view.getNewClientButton().setOnAction(event -> clientiNav.showNuovoCliente());
    }

    private void loadPreviewClients() {
        view.clearClientRows();

        for (ClientePreview cliente : service.getClientiPreview()) {
            view.addClientRow(
                    cliente.name(),
                    cliente.type(),
                    cliente.contact(),
                    cliente.phone(),
                    cliente.email(),
                    cliente.status()
            );
        }
    }

    public ClientiView getView() {
        return view;
    }

    public ClientiNav getClientiNav() {
        return clientiNav;
    }

    public ClientiService getService() {
        return service;
    }
}
