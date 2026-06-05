package com.example.clients.feature.clienti.nuovocliente.controller;

import com.example.clients.app.navigators.DashboardNav;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView;

public class NuovoClienteController {

    private final NuovoClienteView view;
    private final DashboardNav dashboardNav;
    private final NuovoClienteService service;

    public NuovoClienteController(NuovoClienteView view, DashboardNav dashboardNav, NuovoClienteService service) {
        this.view = view;
        this.dashboardNav = dashboardNav;
        this.service = service;
        configureActions();
    }

    private void configureActions() {
        view.getCancelButton().setOnAction(event -> dashboardNav.showClienti());
    }

    public NuovoClienteView getView() {
        return view;
    }

    public DashboardNav getDashboardNav() {
        return dashboardNav;
    }

    public NuovoClienteService getService() {
        return service;
    }
}
