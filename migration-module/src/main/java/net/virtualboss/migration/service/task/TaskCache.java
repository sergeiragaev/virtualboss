package net.virtualboss.migration.service.task;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.service.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class TaskCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object taskNumber, UUID id) {
        cache.put(taskNumber, id);
    }

    @Override
    public UUID get(Object taskNumber) {
        return cache.get(taskNumber);
    }
}