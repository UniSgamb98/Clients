package com.example.clients.app;

import com.example.clients.app.navigators.DashboardNav;
import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.dashboard.controller.DashboardController;
import com.example.clients.feature.dashboard.service.DashboardService;
import com.example.clients.feature.dashboard.view.DashboardView;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class AppController implements DashboardNav {
    private final Stage stage;
    private final AppContainer app;
    private final String cssPath;

    public AppController(Stage stage) {
        this.stage = stage;
        this.app = new AppContainer();
        this.cssPath = Objects.requireNonNull(getClass().getResource("/css/global.css")).toExternalForm();

        showDashboard();
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();
    }

    /*
    -------------------------------------------------------------------------------------------------------------------
    Implemento i metodi di navigazione che verranno passati
    Creo un metodo per ogni view che devo mostrare. Ogni metodo chiama configureHeader per assegnare le funzioni dei
    tasti dello header qua e non nei singoli controller di tutte le view.
     */

    @Override
    public void showDashboard() {
        DashboardView view = new DashboardView();
        configureHeader(view.getHeader());
        configureSidebar(view.getSidebar());
        new DashboardController(view, this, new DashboardService());

        stage.setScene(createSceneWithCSS(view));
        stage.setTitle("Clients - Dashboard");
    }

    @Override
    public void showLaboratory() {
        /*
        LaboratoryView view = new LaboratoryView();
        configureHeader(view.getHeader());
        new LaboratoryController(view, this);

        stage.setScene(createSceneWithCSS(view));
        stage.setTitle("Clients - Laboratorio");*/
    }

    // Creando le Scenes con questo metodo vengono collegate al CSS globale e ai eventuali css specifici per pagina.
    private Scene createSceneWithCSS(Object root, String... extraCss) {
        Scene scene = new Scene((javafx.scene.Parent) root, 900, 700);
        scene.getStylesheets().add(cssPath);

        for (String css : extraCss) {
            String path = Objects.requireNonNull(
                    getClass().getResource(css)
            ).toExternalForm();

            scene.getStylesheets().add(path);
        }
        return scene;
    }

    private void configureHeader(AppHeader header) {
        header.getHomeButton().setOnAction(e -> showDashboard());
        header.getLaboratoryButton().setOnAction(e -> showLaboratory());
    }

    private void configureSidebar(AppSidebar sidebar) {
        sidebar.getDashboardButton().setOnAction(e -> showDashboard());
    }

    public void shutdown() {
        app.shutdown();
        app.database.stop();
    }
}
