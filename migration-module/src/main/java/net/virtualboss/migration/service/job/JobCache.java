package net.virtualboss.migration.service.job;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.service.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class JobCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object jobNumber, UUID id) {
        cache.put(jobNumber, id);
    }

    @Override
    public UUID get(Object jobNumber) {
        return cache.get(jobNumber);
    }
}