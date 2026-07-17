package com.example.clients.feature.impostazioni.view;

import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ImpostazioniView extends BorderPane {

    private static final String[] SETTINGS_AREAS = {
            "Materiale di Consumo",
            "Canali di acquisto",
            "Parco Fresatori",
            "Forni",
            "Ceramica",
            "Fresa"
    };

    private final AppSidebar sidebar = new AppSidebar();

    public ImpostazioniView() {
        setLeft(sidebar);
        setCenter(createContent());
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("settings-content");

        Label title = new Label("Impostazioni");
        title.getStyleClass().add("settings-title");
        Label subtitle = new Label("Gestisci le configurazioni dell'applicazione.");
        subtitle.getStyleClass().add("settings-subtitle");

        GridPane sections = new GridPane();
        sections.setHgap(14);
        sections.setVgap(14);
        sections.getStyleClass().add("settings-sections");
        for (int column = 0; column < 3; column++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100d / 3);
            constraints.setHgrow(Priority.ALWAYS);
            sections.getColumnConstraints().add(constraints);
        }

        for (int index = 0; index < SETTINGS_AREAS.length; index++) {
            sections.add(createSection(SETTINGS_AREAS[index]), index % 3, index / 3);
        }

        content.getChildren().addAll(title, subtitle, sections);
        return content;
    }

    private VBox createSection(String title) {
        VBox section = new VBox();
        section.getStyleClass().add("settings-section");
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("settings-section-title");
        section.getChildren().add(sectionTitle);
        return section;
    }
}
