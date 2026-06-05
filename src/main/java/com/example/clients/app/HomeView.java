package com.example.clients.app;

import com.example.clients.core.ui.AppHeader;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

public class HomeView extends VBox {

    private final AppHeader header;

    public HomeView() {
        header = new AppHeader("Home");

        VBox content = new VBox();

        setSpacing(20);
        setPadding(new Insets(20));

        this.getChildren().addAll(header, content);
    }

    public AppHeader getHeader() { return header; }
}
