package net.virtualboss.common.service;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.mapper.v1.CustomFieldsMapper;
import net.virtualboss.common.model.entity.Field;
import net.virtualboss.common.model.entity.FieldValue;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.repository.FieldRepository;
import net.virtualboss.common.repository.FieldValueRepository;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomFieldService {

    private final FieldRepository fieldRepository;
    private final FieldValueRepository fieldValueRepository;
    private final CustomFieldsMapper customFieldsMapper;

    public Set<FieldValue> createCustomFieldValues(CustomFieldsAndLists customFieldsAndLists, EntityType type) {
        Set<FieldValue> values = new HashSet<>();
        String prefix = formatPrefix(type.name());
        Map<String, String> customFieldsMap = customFieldsMapper.getFieldsMap(customFieldsAndLists, prefix, null);
        customFieldsMap.forEach((caption, fieldValue) -> {
            if (fieldValue == null || fieldValue.isBlank()) {
                return;
            }
            Field field = fieldRepository.findByName(caption)
                    .orElseThrow(() -> new EntityNotFoundException(
                            MessageFormat.format("Field with name {0} not found!", caption)));
            FieldValue value = fieldValueRepository.findByFieldAndCustomValue(field, fieldValue)
                    .orElse(FieldValue.builder()
                            .field(field)
                            .customValue(fieldValue)
                            .build());
            values.add(value);
        });
        return values;
    }

    public CustomFieldsAndLists populateCustomFields(Set<FieldValue> values, EntityType type) {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();
        customFieldsMapper.setCustomFieldsAndListsValues(customFieldsAndLists, values, type.name());
        return customFieldsAndLists;
    }

    private String formatPrefix(String typeName) {
        // Преобразует, например, "ORDER" в "Order"
        return typeName.charAt(0) + typeName.substring(1).toLowerCase();
    }
}