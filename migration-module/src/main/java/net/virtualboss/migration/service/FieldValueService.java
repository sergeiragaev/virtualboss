package net.virtualboss.migration.service;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.repository.FieldValueRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FieldValueService {
    private final FieldValueRepository fieldValueRepository;
    private void setCustomValue(String entityId, String fieldName, String customValue) {
    }

    public void saveCustomFieldValue(Object id, String target, String sourceValue) {
    }
}
