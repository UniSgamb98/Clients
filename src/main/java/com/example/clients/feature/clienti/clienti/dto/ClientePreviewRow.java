package com.example.clients.feature.clienti.clienti.dto;

import java.util.UUID;

public record ClientePreviewRow(
        UUID clienteId,
        ClientePreview preview
) {
}
