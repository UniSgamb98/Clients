package com.example.clients.feature.calendario.view;

import com.example.clients.feature.calendario.dto.CalendarioCall;
import com.example.clients.feature.calendario.dto.CalendarioMonth;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CalendarioMonthView extends VBox {

    private static final String[] WEEK_DAYS = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
    private static final int MAX_VISIBLE_CALLS = 2;

    private final GridPane monthGrid = new GridPane();
    private final Map<LocalDate, VBox> dayCells = new HashMap<>();
    private Consumer<LocalDate> selectDateHandler = date -> { };
    private Consumer<CalendarioCall> openCallHandler = call -> { };

    public CalendarioMonthView() {
        setSpacing(10);
        getStyleClass().add("calendar-panel");
        monthGrid.getStyleClass().add("calendar-month-grid");
        monthGrid.setHgap(8);
        monthGrid.setVgap(8);
        getChildren().add(monthGrid);
    }

    public void showMonth(YearMonth displayedMonth, CalendarioMonth month, LocalDate selectedDate) {
        monthGrid.getChildren().clear();
        dayCells.clear();
        for (int column = 0; column < WEEK_DAYS.length; column++) {
            Label dayHeader = new Label(WEEK_DAYS[column]);
            dayHeader.getStyleClass().add("calendar-day-header");
            monthGrid.add(dayHeader, column, 0);
        }

        int row = 1;
        int column = month.firstColumn();
        for (int day = 1; day <= month.dayCount(); day++) {
            LocalDate date = displayedMonth.atDay(day);
            VBox dayCell = createDayCell(date, month.todayDay() != null && day == month.todayDay(),
                    month.callsByDate().getOrDefault(date, List.of()));
            dayCells.put(date, dayCell);
            monthGrid.add(dayCell, column, row);
            column++;
            if (column == WEEK_DAYS.length) {
                column = 0;
                row++;
            }
        }
        selectDate(selectedDate);
    }

    private VBox createDayCell(LocalDate date, boolean today, List<CalendarioCall> calls) {
        VBox cell = new VBox(6);
        cell.getStyleClass().add("calendar-day-cell");
        if (today) {
            cell.getStyleClass().add("calendar-day-today");
        }

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);
        calls.stream().limit(MAX_VISIBLE_CALLS).map(this::createCallChip).forEach(cell.getChildren()::add);
        if (calls.size() > MAX_VISIBLE_CALLS) {
            Label remaining = new Label("+" + (calls.size() - MAX_VISIBLE_CALLS) + " altre");
            remaining.getStyleClass().add("calendar-more-calls");
            cell.getChildren().add(remaining);
        }
        cell.setOnMouseClicked(event -> {
            selectDate(date);
            selectDateHandler.accept(date);
        });
        return cell;
    }

    private void selectDate(LocalDate date) {
        dayCells.values().forEach(cell -> cell.getStyleClass().remove("calendar-day-selected"));
        VBox selectedCell = dayCells.get(date);
        if (selectedCell != null) {
            selectedCell.getStyleClass().add("calendar-day-selected");
        }
    }

    private Button createCallChip(CalendarioCall call) {
        Button chip = new Button(call.cliente());
        chip.getStyleClass().add("calendar-activity-chip");
        chip.setMaxWidth(Double.MAX_VALUE);
        chip.setOnAction(event -> openCallHandler.accept(call));
        return chip;
    }

    public void setSelectDateHandler(Consumer<LocalDate> handler) {
        selectDateHandler = handler == null ? date -> { } : handler;
    }

    public void setOpenCallHandler(Consumer<CalendarioCall> handler) {
        openCallHandler = handler == null ? call -> { } : handler;
    }
}
