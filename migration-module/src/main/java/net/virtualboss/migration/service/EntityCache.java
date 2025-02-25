package net.virtualboss.migration.service;

import java.util.UUID;

public interface EntityCache {

    void add(Object oldId, UUID id);

    UUID get(Object oldId);
}