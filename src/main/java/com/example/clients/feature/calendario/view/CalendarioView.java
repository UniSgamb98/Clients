package com.example.clients.feature.calendario.view;

import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarioView extends BorderPane {

    private static final Locale ITALIAN = Locale.ITALIAN;
    private static final String[] WEEK_DAYS = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};

    private final AppSidebar sidebar;
    private final Button todayButton;
    private final Button previousMonthButton;
    private final Button nextMonthButton;
    private final Button dayViewButton;
    private final Button weekViewButton;
    private final Button monthViewButton;
    private final Button newActivityButton;
    private final VBox activityList;
    private final Label currentPeriodLabel;
    private final GridPane monthGrid;
    private YearMonth displayedMonth;

    public CalendarioView() {
        sidebar = new AppSidebar();
        todayButton = createSecondaryButton("Oggi");
        previousMonthButton = createSecondaryButton("<");
        nextMonthButton = createSecondaryButton(">");
        dayViewButton = createToggleButton("Giorno");
        weekViewButton = createToggleButton("Settimana");
        monthViewButton = createToggleButton("Mese");
        newActivityButton = createPrimaryButton("+ Nuova attività");
        activityList = new VBox(10);
        activityList.getStyleClass().add("calendar-activity-list");
        displayedMonth = YearMonth.now();
        currentPeriodLabel = new Label();
        currentPeriodLabel.getStyleClass().add("calendar-period-label");
        monthGrid = new GridPane();
        monthGrid.getStyleClass().add("calendar-month-grid");
        monthGrid.setHgap(8);
        monthGrid.setVgap(8);

        previousMonthButton.setOnAction(event -> showMonth(displayedMonth.minusMonths(1)));
        nextMonthButton.setOnAction(event -> showMonth(displayedMonth.plusMonths(1)));
        todayButton.setOnAction(event -> showMonth(YearMonth.now()));
        refreshMonth();

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
                previousMonthButton,
                currentPeriodLabel,
                nextMonthButton,
                spacer,
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

    private void refreshMonth() {
        currentPeriodLabel.setText(formatMonth(displayedMonth));
        monthGrid.getChildren().clear();
        for (int column = 0; column < WEEK_DAYS.length; column++) {
            Label dayHeader = new Label(WEEK_DAYS[column]);
            dayHeader.getStyleClass().add("calendar-day-header");
            monthGrid.add(dayHeader, column, 0);
        }

        LocalDate firstDay = displayedMonth.atDay(1);
        int firstColumn = firstDay.getDayOfWeek().getValue() - 1;
        int row = 1;
        int column = firstColumn;
        LocalDate today = LocalDate.now();
        boolean currentMonth = displayedMonth.equals(YearMonth.from(today));
        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
            VBox cell = createDayCell(day, currentMonth && day == today.getDayOfMonth());
            monthGrid.add(cell, column, row);
            column++;
            if (column == WEEK_DAYS.length) {
                column = 0;
                row++;
            }
        }
    }

    private void showMonth(YearMonth month) {
        displayedMonth = month;
        refreshMonth();
    }

    private VBox createDayCell(int day, boolean today) {
        VBox cell = new VBox(6);
        cell.getStyleClass().add("calendar-day-cell");
        if (today) {
            cell.getStyleClass().add("calendar-day-today");
        }

        Label dayNumber = new Label(String.valueOf(day));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);

        if (day % 4 == 0) {
            cell.getChildren().add(createActivityChip("Follow-up"));
        }
        if (day % 7 == 0) {
            cell.getChildren().add(createActivityChip("Chiamata"));
        }
        return cell;
    }

    private VBox createAgendaPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("calendar-agenda-panel");
        panel.setPrefWidth(280);

        Label title = new Label("Agenda CRM");
        title.getStyleClass().add("calendar-agenda-title");
        Label subtitle = new Label("Priorità operative consigliate per il calendario.");
        subtitle.getStyleClass().add("calendar-agenda-subtitle");

        activityList.getChildren().setAll(
                createAgendaItem("09:00", "Richiamare clienti con prossimo contatto oggi"),
                createAgendaItem("11:30", "Verificare preventivi in attesa"),
                createAgendaItem("15:00", "Follow-up campioni inviati"),
                createAgendaItem("17:00", "Controllare attività scadute")
        );

        panel.getChildren().addAll(title, subtitle, activityList);
        return panel;
    }

    private Label createActivityChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("calendar-activity-chip");
        return chip;
    }

    private HBox createAgendaItem(String time, String text) {
        HBox item = new HBox(10);
        item.getStyleClass().add("calendar-agenda-item");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("calendar-agenda-time");
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("calendar-agenda-text");
        item.getChildren().addAll(timeLabel, textLabel);
        return item;
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

    private Button createToggleButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("calendar-toggle-button");
        return button;
    }

    private String formatMonth(YearMonth month) {
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, ITALIAN);
        return monthName.substring(0, 1).toUpperCase(ITALIAN) + monthName.substring(1) + " " + month.getYear();
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getTodayButton() {
        return todayButton;
    }

    public Button getPreviousMonthButton() {
        return previousMonthButton;
    }

    public Button getNextMonthButton() {
        return nextMonthButton;
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
