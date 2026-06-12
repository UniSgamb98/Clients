package com.example.clients.core.database.query;

import java.util.List;

public interface ClienteLookupQuery {
    LookupValues findValues();

    record LookupValues(
            List<String> tipiCliente,
            List<String> statiTrattativa,
            List<String> telefoni,
            List<String> email
    ) {
        public LookupValues {
            tipiCliente = List.copyOf(tipiCliente);
            statiTrattativa = List.copyOf(statiTrattativa);
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
        }
    }
}
