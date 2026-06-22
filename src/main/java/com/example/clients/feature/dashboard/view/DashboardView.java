package com.example.clients.feature.dashboard.view;

import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DashboardView extends BorderPane {

    private final AppSidebar sidebar;
    private final VBox content;

    public DashboardView() {
        sidebar = new AppSidebar();
        content = new VBox();
        content.setPadding(new Insets(20));
        content.getStyleClass().add("dashboard-content");

        setLeft(sidebar);
        setCenter(content);
    }


    public AppSidebar getSidebar() {
        return sidebar;
    }

    public VBox getContent() {
        return content;
    }
}
