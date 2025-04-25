package net.virtualboss.migration.processor.phone;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class PhoneTypeCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object phoneTypeId, UUID id) {
        cache.put(phoneTypeId, id);
    }

    @Override
    public UUID get(Object phoneTypeId) {
        return cache.get(phoneTypeId);
    }
}