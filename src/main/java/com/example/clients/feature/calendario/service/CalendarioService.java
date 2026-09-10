package com.example.clients.feature.calendario.service;

import com.example.clients.feature.calendario.dto.CalendarioMonth;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarioService {

    private static final Locale ITALIAN = Locale.ITALIAN;

    private final Clock clock;

    public CalendarioService() {
        this(Clock.systemDefaultZone());
    }

    CalendarioService(Clock clock) {
        this.clock = clock;
    }

    public YearMonth currentMonth() {
        return YearMonth.now(clock);
    }

    public CalendarioMonth getMonth(YearMonth month) {
        LocalDate today = LocalDate.now(clock);
        Integer todayDay = month.equals(YearMonth.from(today)) ? today.getDayOfMonth() : null;
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, ITALIAN);
        String periodLabel = monthName.substring(0, 1).toUpperCase(ITALIAN)
                + monthName.substring(1)
                + " "
                + month.getYear();

        return new CalendarioMonth(
                periodLabel,
                month.atDay(1).getDayOfWeek().getValue() - 1,
                month.lengthOfMonth(),
                todayDay
        );
    }
}
