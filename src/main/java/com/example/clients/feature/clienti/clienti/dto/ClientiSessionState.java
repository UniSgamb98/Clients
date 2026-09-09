package com.example.clients.feature.clienti.clienti.dto;

import java.util.UUID;

public record ClientiSessionState(
        ClientiViewState viewState,
        UUID activeSavedViewId,
        ClientiViewState savedBaseline
) {
    public ClientiSessionState {
        viewState = viewState == null ? ClientiViewState.initial() : viewState;
    }
}
