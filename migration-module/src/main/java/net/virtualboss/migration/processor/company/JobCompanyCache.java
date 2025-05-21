package net.virtualboss.migration.processor.company;

import lombok.AllArgsConstructor;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class JobCompanyCache implements EntityCache {
    private final Map<Object, UUID> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Object company, UUID id) {
        cache.put(company, id);
    }

    @Override
    public UUID get(Object company) {
        return cache.get(company);
    }
}