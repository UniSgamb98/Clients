package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.VistaSalvata;
import com.example.clients.core.session.FeatureKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VistaSalvataRepository {

    List<VistaSalvata> findAll(UUID operatoreId, FeatureKey feature);

    Optional<VistaSalvata> findById(UUID id, UUID operatoreId);

    Optional<VistaSalvata> findPredefinita(UUID operatoreId, FeatureKey feature);

    void insert(VistaSalvata vista);

    boolean update(VistaSalvata vista);

    boolean delete(UUID id, UUID operatoreId);

    boolean setPredefinita(UUID id, UUID operatoreId, FeatureKey feature);
}
