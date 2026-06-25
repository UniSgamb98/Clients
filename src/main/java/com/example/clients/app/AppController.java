package com.example.clients.app;

import com.example.clients.core.async.BackgroundExecutor;
import com.example.clients.core.database.query.derby.DerbyStatoTrattativaQuery;
import com.example.clients.core.database.query.derby.DerbyTipoClienteQuery;
import com.example.clients.core.database.service.ClientePersistenceService;
import com.example.clients.core.database.service.CurrentOperatoreService;
import com.example.clients.feature.attivita.controller.AttivitaController;
import com.example.clients.feature.attivita.service.AttivitaService;
import com.example.clients.feature.attivita.view.AttivitaView;
import com.example.clients.feature.auth.login.controller.LoginController;
import com.example.clients.feature.auth.login.navigator.LoginNav;
import com.example.clients.feature.auth.login.service.LoginService;
import com.example.clients.feature.auth.login.view.LoginView;
import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.calendario.view.CalendarioView;
import com.example.clients.feature.clienti.clienti.controller.ClientiController;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.nuovocliente.controller.NuovoClienteController;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView;
import com.example.clients.feature.clienti.schedacliente.controller.SchedaClienteController;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService;
import com.example.clients.feature.clienti.schedacliente.view.SchedaClienteView;
import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.dashboard.controller.DashboardController;
import com.example.clients.feature.dashboard.navigator.DashboardNav;
import com.example.clients.feature.dashboard.service.DashboardService;
import com.example.clients.feature.dashboard.view.DashboardView;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.UUID;

public class AppController implements DashboardNav, ClientiNav, LoginNav {
    private final Stage stage;
    private Scene scene;
    private AppContainer app;
    private final String cssPath;
    private boolean shutdown;

    public AppController(Stage stage) {
        this.stage = stage;
        this.cssPath = Objects.requireNonNull(getClass().getResource("/css/global.css")).toExternalForm();

        showLogin();
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();
    }

    /*
    -------------------------------------------------------------------------------------------------------------------
    Implemento i metodi di navigazione che verranno passati
    Creo un metodo per ogni view che devo mostrare. Ogni metodo configura la sidebar centralizzando la navigazione.
     */

    public void showLogin() {
        LoginView view = new LoginView();
        showView(view);
        stage.setTitle("Clients - Login");

        try {
            ensureAppStarted();
            new LoginController(view, this, new LoginService(app.database));
        } catch (RuntimeException e) {
            view.showError("Caricamento utenti non riuscito: " + safeMessage(e));
            view.getLoginButton().setDisable(true);
        }
    }

    @Override
    public void showDashboardAfterLogin() {
        ensureAppStarted();
        showDashboard();
    }

    @Override
    public void showDashboard() {
        DashboardView view = new DashboardView();
        configureSidebar(view.getSidebar());
        new DashboardController(view, this, new DashboardService());

        showView(view);
        stage.setTitle("Clients - Dashboard");
    }

    @Override
    public void showClienti() {
        ClientiView view = new ClientiView();
        configureSidebar(view.getSidebar());
        ClientiController controller = new ClientiController(view, this, new ClientiService(app.database));

        showView(
                view,
                "/css/features/clienti.css",
                "/css/features/lista-clienti.css"
        );
        stage.setTitle("Clients - Clienti");
        controller.loadPreviewClientsAsync();
    }

    @Override
    public void showNuovoCliente() {
        NuovoClienteView view = new NuovoClienteView();
        configureSidebar(view.getSidebar());
        new NuovoClienteController(
                view,
                this,
                new NuovoClienteService(
                        new ClientePersistenceService(app.database),
                        new CurrentOperatoreService(),
                        new DerbyTipoClienteQuery(app.database),
                        new DerbyStatoTrattativaQuery(app.database)
                )
        );

        showView(
                view,
                "/css/features/clienti.css",
                "/css/features/nuovo-cliente.css"
        );
        stage.setTitle("Clients - Nuovo cliente");
    }

    @Override
    public void showSchedaCliente(UUID clienteId) {
        SchedaClienteView view = new SchedaClienteView();
        configureSidebar(view.getSidebar());
        new SchedaClienteController(view, this, new SchedaClienteService(app.database), clienteId);

        showView(
                view,
                "/css/features/clienti.css",
                "/css/features/scheda-cliente.css"
        );
        stage.setTitle("Clients - Scheda cliente");
    }

    public void showAttivita() {
        AttivitaView view = new AttivitaView();
        configureSidebar(view.getSidebar());
        AttivitaController controller = new AttivitaController(view, new AttivitaService(app.database));

        showView(
                view,
                "/css/features/attivita.css"
        );
        stage.setTitle("Clients - Attività");
        controller.loadAttivitaAsync();
    }

    public void showCalendario() {
        CalendarioView view = new CalendarioView();
        configureSidebar(view.getSidebar());

        showView(
                view,
                "/css/features/calendario.css"
        );
        stage.setTitle("Clients - Calendario");
    }

    // La Scene viene creata solo al primo caricamento: durante la navigazione cambia solo il root.
    private void showView(Parent root, String... extraCss) {
        if (scene == null) {
            scene = new Scene(root, 900, 700);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        scene.getStylesheets().setAll(cssPath);
        for (String css : extraCss) {
            String path = Objects.requireNonNull(
                    getClass().getResource(css)
            ).toExternalForm();

            scene.getStylesheets().add(path);
        }
    }

    private void ensureAppStarted() {
        if (app == null) {
            app = new AppContainer();
        }
    }

    private String safeMessage(RuntimeException e) {
        return e.getMessage() == null || e.getMessage().isBlank() ? "errore imprevisto." : e.getMessage();
    }

    private void configureSidebar(AppSidebar sidebar) {
        sidebar.getDashboardButton().setOnAction(e -> showDashboard());
        sidebar.getClientsButton().setOnAction(e -> showClienti());
        sidebar.getActivitiesButton().setOnAction(e -> showAttivita());
        sidebar.getCalendarButton().setOnAction(e -> showCalendario());
    }

    public void shutdown() {
        if (shutdown) {
            return;
        }

        shutdown = true;
        if (app != null) {
            app.shutdown();
            app.database.stop();
        }
        BackgroundExecutor.shutdown();
    }
}
