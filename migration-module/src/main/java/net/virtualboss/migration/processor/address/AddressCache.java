package net.virtualboss.migration.processor.address;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class AddressCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object addressId, UUID id) {
        cache.put(addressId, id);
    }

    @Override
    public UUID get(Object addressId) {
        return cache.get(addressId);
    }
}