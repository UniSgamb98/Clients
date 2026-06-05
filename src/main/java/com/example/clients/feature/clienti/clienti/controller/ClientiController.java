package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientePreview;
import com.example.clients.feature.clienti.clienti.service.ClientiService.SortColumn;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.navigator.ClientiNav;

import java.util.List;

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
        view.getNameHeaderButton().setOnAction(event -> sortClienti(SortColumn.NAME));
        view.getTypeHeaderButton().setOnAction(event -> sortClienti(SortColumn.TYPE));
        view.getContactHeaderButton().setOnAction(event -> sortClienti(SortColumn.CONTACT));
        view.getPhoneHeaderButton().setOnAction(event -> sortClienti(SortColumn.PHONE));
        view.getEmailHeaderButton().setOnAction(event -> sortClienti(SortColumn.EMAIL));
        view.getStatusHeaderButton().setOnAction(event -> sortClienti(SortColumn.STATUS));
    }

    private void loadPreviewClients() {
        renderClienti(service.getClientiPreview());
    }

    private void sortClienti(SortColumn sortColumn) {
        renderClienti(service.sortClientiBy(sortColumn));
    }

    private void renderClienti(List<ClientePreview> clienti) {
        view.clearClientRows();

        for (ClientePreview cliente : clienti) {
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
