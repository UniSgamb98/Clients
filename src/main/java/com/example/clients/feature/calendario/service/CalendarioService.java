package com.example.clients.feature.calendario.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.query.CalendarioQuery;
import com.example.clients.core.database.query.ClientiFilterQuery;
import com.example.clients.core.database.query.derby.DerbyCalendarioQuery;
import com.example.clients.core.database.query.derby.DerbyClientiFilterQuery;
import com.example.clients.core.database.query.result.OperatoreClienteFilterResult;
import com.example.clients.feature.calendario.dto.CalendarioCall;
import com.example.clients.feature.calendario.dto.CalendarioMonth;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CalendarioService {

    private static final Locale ITALIAN = Locale.ITALIAN;

    private final Clock clock;
    private final CalendarioQuery calendarioQuery;
    private final ClientiFilterQuery clientiFilterQuery;

    public CalendarioService() {
        this(Clock.systemDefaultZone());
    }

    public CalendarioService(Database database) {
        this(Clock.systemDefaultZone(), new DerbyCalendarioQuery(database), new DerbyClientiFilterQuery(database));
    }

    CalendarioService(Clock clock) {
        this(clock, (from, to, operatoreId) -> List.of(), new EmptyClientiFilterQuery());
    }

    CalendarioService(Clock clock, CalendarioQuery calendarioQuery, ClientiFilterQuery clientiFilterQuery) {
        this.clock = clock;
        this.calendarioQuery = calendarioQuery;
        this.clientiFilterQuery = clientiFilterQuery;
    }

    public YearMonth currentMonth() {
        return YearMonth.now(clock);
    }

    public CalendarioMonth getMonth(YearMonth month, UUID operatoreId) {
        LocalDate today = LocalDate.now(clock);
        Integer todayDay = month.equals(YearMonth.from(today)) ? today.getDayOfMonth() : null;
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, ITALIAN);
        String periodLabel = monthName.substring(0, 1).toUpperCase(ITALIAN)
                + monthName.substring(1)
                + " "
                + month.getYear();

        Map<LocalDate, List<CalendarioCall>> callsByDate = calendarioQuery
                .findProssimeChiamate(month.atDay(1), month.atEndOfMonth(), operatoreId)
                .stream()
                .map(call -> new CalendarioCall(
                        call.interazioneId(), call.clienteId(), call.ragioneSociale(),
                        call.operatoreId(), call.operatore(), call.prossimoContatto()))
                .collect(Collectors.groupingBy(CalendarioCall::data));

        return new CalendarioMonth(
                periodLabel,
                month.atDay(1).getDayOfWeek().getValue() - 1,
                month.lengthOfMonth(),
                todayDay,
                callsByDate
        );
    }

    public CalendarioMonth getMonth(YearMonth month) {
        return getMonth(month, null);
    }

    public List<OperatoreFilter> getOperatorFilters() {
        return clientiFilterQuery.findOperatoriConClienti().stream()
                .map(operator -> new OperatoreFilter(operator.id(), operatorLabel(
                        operator.nome(), operator.cognome(), operator.username())))
                .toList();
    }

    private String operatorLabel(String nome, String cognome, String username) {
        String fullName = ((nome == null ? "" : nome.trim()) + " " + (cognome == null ? "" : cognome.trim())).trim();
        return fullName.isBlank() ? username : fullName;
    }

    private static final class EmptyClientiFilterQuery implements ClientiFilterQuery {
        @Override
        public List<OperatoreClienteFilterResult> findOperatoriConClienti() {
            return List.of();
        }

        @Override
        public List<String> findTipiCliente() {
            return List.of();
        }

        @Override
        public List<String> findStatiTrattativa() {
            return List.of();
        }
    }
}
