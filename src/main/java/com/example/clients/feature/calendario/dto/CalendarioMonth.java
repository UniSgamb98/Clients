package com.example.clients.feature.calendario.dto;

public record CalendarioMonth(
        String periodLabel,
        int firstColumn,
        int dayCount,
        Integer todayDay
) {
}
