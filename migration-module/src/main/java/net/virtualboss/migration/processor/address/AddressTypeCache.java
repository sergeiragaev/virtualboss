package net.virtualboss.migration.processor.address;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class AddressTypeCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object addressTypeId, UUID id) {
        cache.put(addressTypeId, id);
    }

    @Override
    public UUID get(Object addressTypeId) {
        return cache.get(addressTypeId);
    }
}