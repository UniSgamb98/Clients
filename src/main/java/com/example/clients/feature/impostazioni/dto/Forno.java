package com.example.clients.feature.impostazioni.dto;

import java.util.UUID;

public record Forno(UUID id, String tecnologia, String anno, String marca, String modello) {
}
