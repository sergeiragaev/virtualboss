package net.virtualboss.migration.processor.phone;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class PhoneCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object phoneId, UUID id) {
        cache.put(phoneId, id);
    }

    @Override
    public UUID get(Object phoneId) {
        return cache.get(phoneId);
    }
}