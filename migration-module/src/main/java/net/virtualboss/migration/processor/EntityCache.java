package net.virtualboss.migration.processor;

import java.util.UUID;

public interface EntityCache {

    void add(Object oldId, UUID id);

    UUID get(Object oldId);
}