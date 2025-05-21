package net.virtualboss.migration.processor.profession;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class ContactProfessionCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object profession, UUID id) {
        cache.put(profession, id);
    }

    @Override
    public UUID get(Object profession) {
        return cache.get(profession);
    }
}