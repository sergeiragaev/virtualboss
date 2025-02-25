package net.virtualboss.migration.service.contact;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.service.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class ContactCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object customerId, UUID id) {
        cache.put(customerId, id);
    }

    @Override
    public UUID get(Object customerId) {
        return cache.get(customerId);
    }
}