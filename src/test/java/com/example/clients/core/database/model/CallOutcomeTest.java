package com.example.clients.core.database.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallOutcomeTest {

    @Test
    void exposesStableCodesAndCommercialStatusMappings() {
        assertEquals(CallOutcome.NON_TROVATO, CallOutcome.fromCode("NON_TROVATO"));
        assertTrue(CallOutcome.NON_TROVATO.statoTrattativa().isEmpty());
        assertEquals("Campione", CallOutcome.CAMPIONE.statoTrattativa().orElseThrow());
        assertEquals("Cliente", CallOutcome.CLIENTE.label());
    }

    @Test
    void toleratesMissingOrUnknownLegacyValues() {
        assertNull(CallOutcome.fromCode(null));
        assertNull(CallOutcome.fromCode(""));
        assertNull(CallOutcome.fromCode("ESITO_FUTURO"));
    }
}
