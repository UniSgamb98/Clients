package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.dto.ClientePreview;
import com.example.clients.feature.clienti.clienti.dto.ClientePreviewRow;
import com.example.clients.feature.clienti.clienti.dto.ClientiPage;
import com.example.clients.feature.clienti.clienti.dto.ClientiSearchRequest;
import com.example.clients.feature.clienti.clienti.dto.ClientiSearchState;
import com.example.clients.feature.clienti.clienti.dto.ClientiViewState;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;
import com.example.clients.feature.clienti.clienti.dto.SortColumn;
import com.example.clients.feature.clienti.clienti.dto.TextFilter;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.clienti.view.ClientiFeedback;
import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.core.database.service.CurrentOperatoreService;
import com.example.clients.core.session.FeatureKey;
import com.example.clients.core.session.FeatureSessionStateStore;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.List;

public class ClientiController {

    private static final int INITIAL_LOAD_SIZE = 30;
    private static final int LOAD_MORE_SIZE = 50;
    private static final Duration SEARCH_DEBOUNCE = Duration.millis(300);

    private final ClientiView view;
    private final ClientiNav clientiNav;
    private final ClientiService service;
    private final ClientiFeedback feedback;
    private final FeatureSessionStateStore sessionStateStore;
    private final CurrentOperatoreService currentOperatoreService;
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);
    private ClientiSearchState searchState = ClientiSearchState.initial(INITIAL_LOAD_SIZE);
    private long loadVersion;
    private boolean clearingFilters;
    private boolean loadingPage;
    private boolean hasNextPage;
    private int loadedRows;
    private boolean restoringFilters;
    private int pendingFilterLoads;

    public ClientiController(
            ClientiView view,
            ClientiNav clientiNav,
            ClientiService service,
            FeatureSessionStateStore sessionStateStore,
            CurrentOperatoreService currentOperatoreService
    ) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        this.sessionStateStore = sessionStateStore;
        this.currentOperatoreService = currentOperatoreService;
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
        view.onScrollNearBottom(this::loadNextPage);
    }

    public void loadPreviewClientsAsync() {
        ClientiViewState savedState = sessionStateStore.find(
                currentOperatoreService.currentOperatoreId(),
                FeatureKey.CLIENTI,
                ClientiViewState.class
        ).orElseGet(ClientiViewState::initial);
        searchState = savedState.toSearchState(INITIAL_LOAD_SIZE);
        restoringFilters = true;
        pendingFilterLoads = 3;
        loadFiltersAsync();
    }

    private void loadFiltersAsync() {
        AsyncLoader.run(
                service::getOperatorFilters,
                operators -> completeFilterLoad(() -> view.setOperatorFilters(operators)),
                error -> completeFilterLoad(() -> view.setOperatorFilters(List.of()))
        );
        AsyncLoader.run(
                service::getTipoClienteFilters,
                types -> completeFilterLoad(() -> view.setTypeFilters(types)),
                error -> completeFilterLoad(() -> view.setTypeFilters(List.of()))
        );
        AsyncLoader.run(
                service::getStatoTrattativaFilters,
                statuses -> completeFilterLoad(() -> view.setStatusFilters(statuses)),
                error -> completeFilterLoad(() -> view.setStatusFilters(List.of()))
        );
    }

    private void completeFilterLoad(Runnable updateFilterOptions) {
        updateFilterOptions.run();
        pendingFilterLoads--;
        if (pendingFilterLoads == 0) {
            ClientiViewState restoredState = view.applySearchState(ClientiViewState.from(searchState));
            searchState = restoredState.toSearchState(INITIAL_LOAD_SIZE);
            restoringFilters = false;
            rememberSearchState();
            reloadClients();
        }
    }

    private void searchClienti(String searchText) {
        if (clearingFilters || restoringFilters) {
            return;
        }
        searchState = searchState.withSearchText(searchText);
        rememberSearchState();
        searchDebounce.stop();
        searchDebounce.setOnFinished(event -> reloadClients());
        searchDebounce.playFromStart();
    }

    private void filterByOperatore(OperatoreFilter operatoreFilter) {
        if (clearingFilters || restoringFilters) {
            return;
        }
        searchState = searchState.withOperatore(operatoreFilter);
        rememberSearchState();
        reloadClients();
    }

    private void filterByTipoCliente(TextFilter filter) {
        if (clearingFilters || restoringFilters) {
            return;
        }
        searchState = searchState.withTipologia(filter);
        rememberSearchState();
        reloadClients();
    }

    private void filterByStatoTrattativa(TextFilter filter) {
        if (clearingFilters || restoringFilters) {
            return;
        }
        searchState = searchState.withStato(filter);
        rememberSearchState();
        reloadClients();
    }

    private void clearFilters() {
        clearingFilters = true;
        searchDebounce.stop();
        view.clearFilters();
        searchState = ClientiSearchState.initial(INITIAL_LOAD_SIZE);
        clearingFilters = false;
        rememberSearchState();
        reloadClients();
    }

    private void showSaveSearchUnavailable() {
        feedback.showFeatureInDevelopment("Salvataggio ricerca");
    }

    private void sortClienti(SortColumn sortColumn) {
        searchState = searchState.togglingSort(sortColumn);
        rememberSearchState();
        reloadClients();
    }

    private void rememberSearchState() {
        sessionStateStore.save(
                currentOperatoreService.currentOperatoreId(),
                FeatureKey.CLIENTI,
                ClientiViewState.from(searchState)
        );
    }

    private void reloadClients() {
        hasNextPage = false;
        loadedRows = 0;
        searchState = searchState.withPageSize(INITIAL_LOAD_SIZE);
        loadPage(0, INITIAL_LOAD_SIZE, false);
    }

    private void loadNextPage() {
        if (hasNextPage && !loadingPage) {
            loadPage(loadedRows, LOAD_MORE_SIZE, true);
        }
    }

    private void loadPage(int offset, int pageSize, boolean append) {
        if (append && (!hasNextPage || loadingPage)) {
            return;
        }
        searchState = searchState.withPageSize(pageSize).withOffset(offset);
        ClientiSearchRequest request = searchState.toRequest();
        long version = ++loadVersion;
        loadingPage = true;
        if (append) {
            view.showLoadingMore();
        } else {
            view.showLoading();
        }

        AsyncLoader.run(
                () -> service.getClientiPreview(request),
                clientiPage -> {
                    if (version == loadVersion) {
                        renderClienti(clientiPage, append);
                        loadingPage = false;
                    }
                },
                error -> {
                    if (version == loadVersion) {
                        loadingPage = false;
                        view.showError("Caricamento clienti non riuscito.");
                    }
                }
        );
    }

    private void renderClienti(ClientiPage page, boolean append) {
        searchState = searchState.withOffset(page.offset());
        hasNextPage = page.hasNextPage();
        view.setResultsCount(page.totalRows());

        if (page.rows().isEmpty() && !append) {
            view.showEmpty();
            return;
        }

        if (!append) {
            view.clearClientRows();
            loadedRows = 0;
        }

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
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    clientiNav.showSchedaCliente(cliente.clienteId());
                    return;
                }
                view.openClientDetails(preview, row, () -> clientiNav.showSchedaCliente(cliente.clienteId()));
            });
        }
        loadedRows += page.rows().size();

        if (hasNextPage) {
            view.showLoadMoreAvailable();
        } else {
            view.showAllResultsLoaded();
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
