package net.virtualboss.common.service;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.exception.EntityNotFoundException;
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

    public Set<FieldValue> createCustomList(CustomFieldsAndLists customFieldsAndLists, EntityType type) {
        Set<FieldValue> values = new HashSet<>();
        String prefix = type.name().charAt(0) + type.name().toLowerCase().substring(1);
        Map<String, String> customFieldsMap =
                CustomFieldsAndLists.getFieldsMap(customFieldsAndLists, prefix, null);
        for (Map.Entry<String, String> entry : customFieldsMap.entrySet()) {
            String fieldCaption = entry.getKey();
            if (entry.getValue() == null) continue;
            String fieldValue = entry.getValue();
            if (!fieldValue.isBlank()) {
                Field field = fieldRepository
                        .findByName(fieldCaption)
                        .orElseThrow(() -> new EntityNotFoundException(
                                MessageFormat.format("Field with name {0} not found!", fieldCaption)));
                FieldValue value = fieldValueRepository
                        .findByFieldAndValue(field, fieldValue).orElse(
                                FieldValue.builder()
                                        .field(field)
                                        .value(fieldValue)
                                        .build()
                        );

                values.add(value);
            }
        }
        return values;
    }

    public CustomFieldsAndLists setCustomFieldsAndLists(Set<FieldValue> values, EntityType type) {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();
        CustomFieldsAndLists.setCustomFieldsAndListsValues(customFieldsAndLists, values, type.name());
        return customFieldsAndLists;
    }
}