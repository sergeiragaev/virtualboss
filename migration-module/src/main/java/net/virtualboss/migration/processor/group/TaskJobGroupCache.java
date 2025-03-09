package net.virtualboss.migration.processor.group;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class TaskJobGroupCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object name, UUID id) {
        cache.put(name, id);
    }

    @Override
    public UUID get(Object name) {
        return cache.get(name);
    }
}