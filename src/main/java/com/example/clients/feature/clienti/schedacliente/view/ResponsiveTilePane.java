package com.example.clients.feature.clienti.schedacliente.view;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

/**
 * TilePane that keeps a stable one/two column layout based on the available width.
 */
class ResponsiveTilePane extends TilePane {

    private final double gap;
    private final double twoColumnBreakpoint;

    ResponsiveTilePane(double gap, double twoColumnBreakpoint) {
        super(gap, gap);
        this.gap = gap;
        this.twoColumnBreakpoint = twoColumnBreakpoint;
        getStyleClass().add("client-profile-responsive-tile-pane");
        setMaxWidth(Double.MAX_VALUE);
        setPrefColumns(1);
        widthProperty().addListener((observable, oldWidth, newWidth) -> updateTileSizing(newWidth.doubleValue()));
    }

    void addStretchingTile(Node node) {
        if (node instanceof Region region) {
            region.setMinWidth(0);
            region.setMaxWidth(Double.MAX_VALUE);
        }
        getChildren().add(node);
        updateTileSizing(getWidth());
    }

    private void updateTileSizing(double width) {
        if (width <= 0) {
            return;
        }
        boolean twoColumns = width >= twoColumnBreakpoint;
        setPrefColumns(twoColumns ? 2 : 1);
        setPrefTileWidth(twoColumns ? (width - gap) / 2 : width);
    }
}
