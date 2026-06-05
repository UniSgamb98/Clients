package com.example.clients.feature.clienti.controller;

import com.example.clients.app.navigators.DashboardNav;
import com.example.clients.feature.clienti.service.ClientiService;
import com.example.clients.feature.clienti.service.ClientiService.ClientePreview;
import com.example.clients.feature.clienti.view.ClientiView;

public class ClientiController {

    private final ClientiView view;
    private final DashboardNav dashboardNav;
    private final ClientiService service;

    public ClientiController(ClientiView view, DashboardNav dashboardNav, ClientiService service) {
        this.view = view;
        this.dashboardNav = dashboardNav;
        this.service = service;
        loadPreviewClients();
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

    public DashboardNav getDashboardNav() {
        return dashboardNav;
    }

    public ClientiService getService() {
        return service;
    }
}
