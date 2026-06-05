package com.example.clients.feature.dashboard.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DashboardView extends BorderPane {

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final VBox content;

    public DashboardView() {
        header = new AppHeader("Dashboard");
        sidebar = new AppSidebar();
        content = new VBox();
        content.setPadding(new Insets(20));
        content.getStyleClass().add("dashboard-content");

        setTop(header);
        setLeft(sidebar);
        setCenter(content);
    }

    public AppHeader getHeader() {
        return header;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public VBox getContent() {
        return content;
    }
}
