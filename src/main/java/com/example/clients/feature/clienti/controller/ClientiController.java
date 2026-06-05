package com.example.clients.feature.clienti.controller;

import com.example.clients.app.navigators.DashboardNav;
import com.example.clients.feature.clienti.service.ClientiService;
import com.example.clients.feature.clienti.view.ClientiView;

public class ClientiController {

    private final ClientiView view;
    private final DashboardNav dashboardNav;
    private final ClientiService service;

    public ClientiController(ClientiView view, DashboardNav dashboardNav, ClientiService service) {
        this.view = view;
        this.dashboardNav = dashboardNav;
        this.service = service;
    }

    public ClientiView getView() {
        return view;
    }

    public DashboardNav getDashboardNav() {
        return dashboardNav;
    }

    public ClientiService getService() {
        return service;
    }
}
