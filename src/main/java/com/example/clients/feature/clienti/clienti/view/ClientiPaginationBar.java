package com.example.clients.feature.clienti.clienti.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ClientiPaginationBar extends StackPane {

    private final Button previousButton = createNavigationButton("‹");
    private final Button nextButton = createNavigationButton("›");
    private final ChoiceBox<Integer> rowsPerPageChoiceBox = new ChoiceBox<>();
    private final HBox pageButtons = new HBox(6);
    private final Label resultsRangeLabel = new Label("0 risultati");
    private IntConsumer pageSelectionHandler = page -> { };

    public ClientiPaginationBar() {
        getStyleClass().add("clients-pagination-bar");
        setMaxWidth(Double.MAX_VALUE);
        rowsPerPageChoiceBox.getItems().addAll(10, 25, 50, 100);
        rowsPerPageChoiceBox.getSelectionModel().select(Integer.valueOf(10));
        rowsPerPageChoiceBox.getStyleClass().add("clients-rows-per-page-choice");
        pageButtons.getStyleClass().add("clients-page-number-buttons");
        resultsRangeLabel.getStyleClass().add("clients-pagination-label");
        Label rowsLabel = new Label("Righe per pagina");
        rowsLabel.getStyleClass().add("clients-pagination-label");
        HBox rowsPerPage = new HBox(10, rowsLabel, rowsPerPageChoiceBox);
        rowsPerPage.getStyleClass().add("clients-pagination-side");
        HBox navigation = new HBox(6, previousButton, pageButtons, nextButton);
        navigation.getStyleClass().add("clients-page-navigation");
        StackPane.setAlignment(rowsPerPage, Pos.CENTER_LEFT);
        StackPane.setAlignment(navigation, Pos.CENTER);
        StackPane.setAlignment(resultsRangeLabel, Pos.CENTER_RIGHT);
        getChildren().addAll(rowsPerPage, navigation, resultsRangeLabel);
        setNavigationDisabled(true);
    }

    public void onPaginationRequested(Runnable previousAction, Runnable nextAction, IntConsumer pageAction) {
        previousButton.setOnAction(event -> previousAction.run());
        nextButton.setOnAction(event -> nextAction.run());
        pageSelectionHandler = pageAction == null ? page -> { } : pageAction;
    }

    public void onPageSizeChanged(Consumer<Integer> action) {
        rowsPerPageChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> action.accept(newValue));
    }

    public void render(int page, int totalPages, boolean hasPreviousPage, boolean hasNextPage, long totalRows, int pageSize) {
        previousButton.setDisable(!hasPreviousPage);
        nextButton.setDisable(!hasNextPage);
        pageButtons.getChildren().clear();
        int firstPage = Math.max(0, Math.min(page - 2, totalPages - 5));
        for (int pageIndex = firstPage; pageIndex < Math.min(firstPage + 5, totalPages); pageIndex++) {
            Button button = new Button(String.valueOf(pageIndex + 1));
            button.getStyleClass().add("clients-page-number-button");
            if (pageIndex == page) {
                button.getStyleClass().add("clients-page-number-button-active");
            }
            int selectedPage = pageIndex;
            button.setOnAction(event -> pageSelectionHandler.accept(selectedPage));
            pageButtons.getChildren().add(button);
        }
        long firstResult = totalRows == 0 ? 0 : (long) page * pageSize + 1;
        long lastResult = Math.min((long) (page + 1) * pageSize, totalRows);
        resultsRangeLabel.setText(firstResult + "–" + lastResult + " di " + totalRows + " risultati");
    }

    public void setNavigationDisabled(boolean disabled) {
        previousButton.setDisable(disabled);
        nextButton.setDisable(disabled);
    }

    private Button createNavigationButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("clients-filter-button");
        return button;
    }
}
