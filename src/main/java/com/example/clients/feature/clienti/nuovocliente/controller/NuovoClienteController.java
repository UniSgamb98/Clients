package com.example.clients.feature.clienti.nuovocliente.controller;

import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView;

public class NuovoClienteController {

    private final NuovoClienteView view;
    private final ClientiNav clientiNav;
    private final NuovoClienteService service;

    public NuovoClienteController(NuovoClienteView view, ClientiNav clientiNav, NuovoClienteService service) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
    }

    private void configureActions() {
        view.getCancelButton().setOnAction(event -> clientiNav.showClienti());
    }

    public NuovoClienteView getView() {
        return view;
    }

    public ClientiNav getClientiNav() {
        return clientiNav;
    }

    public NuovoClienteService getService() {
        return service;
    }
}
