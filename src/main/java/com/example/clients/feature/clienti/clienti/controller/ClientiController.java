package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientePreview;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientePreviewRow;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientiPage;
import com.example.clients.feature.clienti.clienti.service.ClientiService.ClientiSearchRequest;
import com.example.clients.feature.clienti.clienti.service.ClientiService.SortColumn;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.navigator.ClientiNav;

public class ClientiController {

    private static final int PAGE_SIZE = 50;

    private final ClientiView view;
    private final ClientiNav clientiNav;
    private final ClientiService service;
    private SortColumn currentSortColumn = SortColumn.NAME;
    private boolean ascending = true;
    private int currentPage;
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
        view.getPreviousPageButton().setOnAction(event -> loadPage(currentPage - 1));
        view.getNextPageButton().setOnAction(event -> loadPage(currentPage + 1));
    }

    public void loadPreviewClientsAsync() {
        loadPage(0);
    }

    private void sortClienti(SortColumn sortColumn) {
        if (sortColumn == currentSortColumn) {
            ascending = !ascending;
        } else {
            currentSortColumn = sortColumn;
            ascending = true;
        }
        loadPage(0);
    }

    private void loadPage(int page) {
        currentPage = Math.max(0, page);
        ClientiSearchRequest request = new ClientiSearchRequest(currentPage, PAGE_SIZE, currentSortColumn, ascending);
        long version = ++loadVersion;
        view.showLoading();
        view.setPaginationDisabled(true);

        AsyncLoader.run(
                () -> service.getClientiPreview(request),
                clientiPage -> {
                    if (version == loadVersion) {
                        renderClienti(clientiPage);
                    }
                },
                error -> {
                    if (version == loadVersion) {
                        view.showError("Caricamento clienti non riuscito.");
                        view.setPaginationDisabled(true);
                    }
                }
        );
    }

    private void renderClienti(ClientiPage page) {
        currentPage = page.page();
        view.renderPagination(page.page(), page.totalPages(), page.hasPreviousPage(), page.hasNextPage());

        if (page.rows().isEmpty()) {
            view.showEmpty();
            return;
        }

        view.clearClientRows();

        for (ClientePreviewRow cliente : page.rows()) {
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
}
