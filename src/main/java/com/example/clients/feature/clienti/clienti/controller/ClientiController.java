package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.dto.ClientePreview;
import com.example.clients.feature.clienti.clienti.dto.ClientePreviewRow;
import com.example.clients.feature.clienti.clienti.dto.ClientiPage;
import com.example.clients.feature.clienti.clienti.dto.ClientiSearchRequest;
import com.example.clients.feature.clienti.clienti.dto.ClientiSearchState;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;
import com.example.clients.feature.clienti.clienti.dto.SortColumn;
import com.example.clients.feature.clienti.clienti.dto.TextFilter;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.clienti.view.ClientiFeedback;
import com.example.clients.feature.clienti.navigator.ClientiNav;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.List;

public class ClientiController {

    private static final int ALL_RESULTS_PAGE_SIZE = Integer.MAX_VALUE;
    private static final Duration SEARCH_DEBOUNCE = Duration.millis(300);

    private final ClientiView view;
    private final ClientiNav clientiNav;
    private final ClientiService service;
    private final ClientiFeedback feedback;
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);
    private ClientiSearchState searchState = ClientiSearchState.initial(ALL_RESULTS_PAGE_SIZE);
    private long loadVersion;
    private boolean clearingFilters;

    public ClientiController(ClientiView view, ClientiNav clientiNav, ClientiService service) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        this.feedback = new ClientiFeedback();
        configureActions();
    }

    private void configureActions() {
        view.onNewClient(clientiNav::showNuovoCliente);
        view.onSortRequested(this::sortClienti);
        view.onSearchChanged(this::searchClienti);
        view.onOperatoreFilterChanged(this::filterByOperatore);
        view.onTipologiaFilterChanged(this::filterByTipoCliente);
        view.onStatoFilterChanged(this::filterByStatoTrattativa);
        view.onClearFilters(this::clearFilters);
        view.onSaveSearch(this::showSaveSearchUnavailable);
    }

    public void loadPreviewClientsAsync() {
        loadFiltersAsync();
        loadPage(0);
    }

    private void loadFiltersAsync() {
        AsyncLoader.run(
                service::getOperatorFilters,
                view::setOperatorFilters,
                error -> view.setOperatorFilters(List.of())
        );
        AsyncLoader.run(
                service::getTipoClienteFilters,
                view::setTypeFilters,
                error -> view.setTypeFilters(List.of())
        );
        AsyncLoader.run(
                service::getStatoTrattativaFilters,
                view::setStatusFilters,
                error -> view.setStatusFilters(List.of())
        );
    }

    private void searchClienti(String searchText) {
        searchState = searchState.withSearchText(searchText);
        if (clearingFilters) {
            return;
        }
        searchDebounce.stop();
        searchDebounce.setOnFinished(event -> loadPage(0));
        searchDebounce.playFromStart();
    }

    private void filterByOperatore(OperatoreFilter operatoreFilter) {
        searchState = searchState.withOperatore(operatoreFilter);
        if (!clearingFilters) {
            loadPage(0);
        }
    }

    private void filterByTipoCliente(TextFilter filter) {
        searchState = searchState.withTipologia(filter);
        if (!clearingFilters) {
            loadPage(0);
        }
    }

    private void filterByStatoTrattativa(TextFilter filter) {
        searchState = searchState.withStato(filter);
        if (!clearingFilters) {
            loadPage(0);
        }
    }

    private void clearFilters() {
        clearingFilters = true;
        searchDebounce.stop();
        view.clearFilters();
        searchState = ClientiSearchState.initial(ALL_RESULTS_PAGE_SIZE);
        clearingFilters = false;
        loadPage(0);
    }

    private void showSaveSearchUnavailable() {
        feedback.showFeatureInDevelopment("Salvataggio ricerca");
    }

    private void sortClienti(SortColumn sortColumn) {
        searchState = searchState.togglingSort(sortColumn);
        loadPage(0);
    }

    private void loadPage(int page) {
        searchState = searchState.withPage(page);
        ClientiSearchRequest request = searchState.toRequest();
        long version = ++loadVersion;
        view.showLoading();

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
                    }
                }
        );
    }

    private void renderClienti(ClientiPage page) {
        searchState = searchState.withPage(page.page());
        view.setResultsCount(page.totalRows());

        if (page.rows().isEmpty()) {
            view.showEmpty();
            return;
        }

        view.clearClientRows();

        for (ClientePreviewRow cliente : page.rows()) {
            ClientePreview preview = cliente.preview();
            var row = view.addClientRow(
                    preview.name(),
                    preview.type(),
                    preview.contact(),
                    preview.operator(),
                    preview.status(),
                    preview.lastContact(),
                    this::showRowActionsUnavailable
            );
            row.setOnMouseClicked(event -> view.openClientDetails(preview, row, () -> clientiNav.showSchedaCliente(cliente.clienteId())));
        }
    }

    private void showRowActionsUnavailable() {
        feedback.showFeatureInDevelopment("Azioni cliente");
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
