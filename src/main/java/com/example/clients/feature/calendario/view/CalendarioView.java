package com.example.clients.feature.calendario.view;

import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.calendario.dto.CalendarioCall;
import com.example.clients.feature.calendario.dto.CalendarioMonth;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CalendarioView extends BorderPane {

    private static final String[] WEEK_DAYS = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
    private static final int MAX_VISIBLE_CALLS = 2;
    private static final DateTimeFormatter AGENDA_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM", java.util.Locale.ITALIAN);

    private final AppSidebar sidebar;
    private final Button todayButton;
    private final Button previousYearButton;
    private final Button previousMonthButton;
    private final Button nextMonthButton;
    private final Button nextYearButton;
    private final Button dayViewButton;
    private final Button weekViewButton;
    private final Button monthViewButton;
    private final Button newActivityButton;
    private final ChoiceBox<OperatoreFilter> operatorFilterChoiceBox;
    private final VBox activityList;
    private final Label agendaTitle;
    private final Label agendaSubtitle;
    private final Label currentPeriodLabel;
    private final GridPane monthGrid;
    private Map<LocalDate, List<CalendarioCall>> callsByDate = Map.of();
    private Consumer<CalendarioCall> openCallHandler = call -> { };

    public CalendarioView() {
        sidebar = new AppSidebar();
        todayButton = createSecondaryButton("Oggi");
        previousYearButton = createNavigationButton("/\\", "Anno precedente");
        previousMonthButton = createSecondaryButton("<");
        nextMonthButton = createSecondaryButton(">");
        nextYearButton = createNavigationButton("\\/", "Anno successivo");
        dayViewButton = createToggleButton("Giorno");
        weekViewButton = createToggleButton("Settimana");
        monthViewButton = createToggleButton("Mese");
        newActivityButton = createPrimaryButton("+ Nuova attività");
        operatorFilterChoiceBox = new ChoiceBox<>();
        operatorFilterChoiceBox.getStyleClass().add("calendar-operator-filter");
        activityList = new VBox(10);
        activityList.getStyleClass().add("calendar-activity-list");
        agendaTitle = new Label("Agenda CRM");
        agendaTitle.getStyleClass().add("calendar-agenda-title");
        agendaSubtitle = new Label();
        agendaSubtitle.getStyleClass().add("calendar-agenda-subtitle");
        currentPeriodLabel = new Label();
        currentPeriodLabel.getStyleClass().add("calendar-period-label");
        monthGrid = new GridPane();
        monthGrid.getStyleClass().add("calendar-month-grid");
        monthGrid.setHgap(8);
        monthGrid.setVgap(8);

        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("calendar-content");

        content.getChildren().addAll(
                createTitleBar(),
                createToolbar(),
                createCalendarBody()
        );
        return content;
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().add("calendar-title-bar");

        VBox titleBox = new VBox(4);
        Label title = new Label("Calendario");
        title.getStyleClass().add("calendar-title");
        Label subtitle = new Label("Pianifica chiamate, follow-up, appuntamenti e scadenze commerciali.");
        subtitle.getStyleClass().add("calendar-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleBox, spacer, newActivityButton);
        return titleBar;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("calendar-toolbar");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        monthViewButton.getStyleClass().add("calendar-toggle-selected");
        toolbar.getChildren().addAll(
                todayButton,
                previousYearButton,
                previousMonthButton,
                currentPeriodLabel,
                nextMonthButton,
                nextYearButton,
                spacer,
                new Label("Operatore"),
                operatorFilterChoiceBox,
                dayViewButton,
                weekViewButton,
                monthViewButton
        );
        return toolbar;
    }

    private HBox createCalendarBody() {
        HBox body = new HBox(16);
        body.getStyleClass().add("calendar-body");
        body.getChildren().addAll(createMonthPanel(), createAgendaPanel());
        HBox.setHgrow(body.getChildren().get(0), Priority.ALWAYS);
        return body;
    }

    private VBox createMonthPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("calendar-panel");
        panel.getChildren().add(monthGrid);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    public void showMonth(YearMonth displayedMonth, CalendarioMonth month) {
        currentPeriodLabel.setText(month.periodLabel());
        callsByDate = month.callsByDate();
        monthGrid.getChildren().clear();
        for (int column = 0; column < WEEK_DAYS.length; column++) {
            Label dayHeader = new Label(WEEK_DAYS[column]);
            dayHeader.getStyleClass().add("calendar-day-header");
            monthGrid.add(dayHeader, column, 0);
        }

        int row = 1;
        int column = month.firstColumn();
        for (int day = 1; day <= month.dayCount(); day++) {
            LocalDate date = displayedMonth.atDay(day);
            VBox cell = createDayCell(date, month.todayDay() != null && day == month.todayDay());
            monthGrid.add(cell, column, row);
            column++;
            if (column == WEEK_DAYS.length) {
                column = 0;
                row++;
            }
        }
        LocalDate initialDate = month.todayDay() == null
                ? displayedMonth.atDay(1)
                : displayedMonth.atDay(month.todayDay());
        showAgenda(initialDate);
    }

    private VBox createDayCell(LocalDate date, boolean today) {
        VBox cell = new VBox(6);
        cell.getStyleClass().add("calendar-day-cell");
        if (today) {
            cell.getStyleClass().add("calendar-day-today");
        }

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);

        List<CalendarioCall> calls = callsByDate.getOrDefault(date, List.of());
        calls.stream().limit(MAX_VISIBLE_CALLS).map(this::createActivityChip).forEach(cell.getChildren()::add);
        if (calls.size() > MAX_VISIBLE_CALLS) {
            Label remaining = new Label("+" + (calls.size() - MAX_VISIBLE_CALLS) + " altre");
            remaining.getStyleClass().add("calendar-more-calls");
            cell.getChildren().add(remaining);
        }
        cell.setOnMouseClicked(event -> showAgenda(date));
        return cell;
    }

    private VBox createAgendaPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("calendar-agenda-panel");
        panel.setPrefWidth(280);

        panel.getChildren().addAll(agendaTitle, agendaSubtitle, activityList);
        return panel;
    }

    private Button createActivityChip(CalendarioCall call) {
        Button chip = new Button(call.cliente());
        chip.getStyleClass().add("calendar-activity-chip");
        chip.setMaxWidth(Double.MAX_VALUE);
        chip.setOnAction(event -> openCallHandler.accept(call));
        return chip;
    }

    private HBox createAgendaItem(CalendarioCall call) {
        HBox item = new HBox(10);
        item.getStyleClass().add("calendar-agenda-item");
        Label operatorLabel = new Label(call.operatore());
        operatorLabel.getStyleClass().add("calendar-agenda-time");
        Label textLabel = new Label(call.cliente());
        textLabel.getStyleClass().add("calendar-agenda-text");
        item.getChildren().addAll(operatorLabel, textLabel);
        item.setOnMouseClicked(event -> openCallHandler.accept(call));
        return item;
    }

    private void showAgenda(LocalDate date) {
        List<CalendarioCall> calls = callsByDate.getOrDefault(date, List.of());
        agendaSubtitle.setText(AGENDA_DATE_FORMATTER.format(date));
        if (calls.isEmpty()) {
            Label empty = new Label("Nessuna chiamata pianificata.");
            empty.getStyleClass().add("calendar-agenda-empty");
            activityList.getChildren().setAll(empty);
            return;
        }
        activityList.getChildren().setAll(calls.stream().map(this::createAgendaItem).toList());
    }

    public void setOperatorFilters(List<OperatoreFilter> filters) {
        operatorFilterChoiceBox.getItems().setAll(OperatoreFilter.empty());
        operatorFilterChoiceBox.getItems().addAll(filters);
        operatorFilterChoiceBox.getSelectionModel().selectFirst();
    }

    public void setOpenCallHandler(Consumer<CalendarioCall> handler) {
        openCallHandler = handler == null ? call -> { } : handler;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("calendar-primary-button");
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("calendar-secondary-button");
        return button;
    }

    private Button createNavigationButton(String text, String description) {
        Button button = createSecondaryButton(text);
        button.setAccessibleText(description);
        button.setTooltip(new Tooltip(description));
        return button;
    }

    private Button createToggleButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("calendar-toggle-button");
        return button;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public ChoiceBox<OperatoreFilter> getOperatorFilterChoiceBox() {
        return operatorFilterChoiceBox;
    }

    public Button getTodayButton() {
        return todayButton;
    }

    public Button getPreviousMonthButton() {
        return previousMonthButton;
    }

    public Button getPreviousYearButton() {
        return previousYearButton;
    }

    public Button getNextMonthButton() {
        return nextMonthButton;
    }

    public Button getNextYearButton() {
        return nextYearButton;
    }

    public Button getDayViewButton() {
        return dayViewButton;
    }

    public Button getWeekViewButton() {
        return weekViewButton;
    }

    public Button getMonthViewButton() {
        return monthViewButton;
    }

    public Button getNewActivityButton() {
        return newActivityButton;
    }

    public VBox getActivityList() {
        return activityList;
    }
}
