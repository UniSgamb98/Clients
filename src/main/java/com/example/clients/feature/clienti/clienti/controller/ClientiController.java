package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientePreview;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientePreviewRow;
import com.example.clients.feature.clienti.clienti.service.ClientiService.SortColumn;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.navigator.ClientiNav;

import java.util.List;

public class ClientiController {

    private final ClientiView view;
    private final ClientiNav clientiNav;
    private final ClientiService service;
    private long loadVersion;

    public ClientiController(ClientiView view, ClientiNav clientiNav, ClientiService service) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
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

    public void loadPreviewClientsAsync() {
        loadClientiAsync(service::getClientiPreview);
    }

    private void sortClienti(SortColumn sortColumn) {
        loadClientiAsync(() -> service.sortClientiBy(sortColumn));
    }

    private void loadClientiAsync(ClientiLoadAction action) {
        long version = ++loadVersion;
        view.showLoading();

        AsyncLoader.run(
                action::load,
                clienti -> {
                    if (version == loadVersion) {
                        renderClienti(clienti);
                    }
                },
                error -> {
                    if (version == loadVersion) {
                        view.showError("Caricamento clienti non riuscito.");
                    }
                }
        );
    }

    private void renderClienti(List<ClientePreviewRow> clienti) {
        if (clienti.isEmpty()) {
            view.showEmpty();
            return;
        }

        view.clearClientRows();

        for (ClientePreviewRow cliente : clienti) {
            ClientePreview preview = cliente.preview();
            view.addClientRow(
                    preview.name(),
                    preview.type(),
                    preview.contact(),
                    preview.phone(),
                    preview.email(),
                    preview.status()
            ).setOnMouseClicked(event -> clientiNav.showSchedaCliente(cliente.clienteId()));
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

    @FunctionalInterface
    private interface ClientiLoadAction {
        List<ClientePreviewRow> load();
    }
}
