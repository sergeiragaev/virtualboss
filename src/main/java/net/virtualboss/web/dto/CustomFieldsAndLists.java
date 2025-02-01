package net.virtualboss.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.virtualboss.model.entity.FieldValue;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Builder
@Data
public class CustomFieldsAndLists {
    @JsonProperty("CustomField1")
    @Builder.Default
    private String customField1 = "";
    @JsonProperty("CustomField2")
    @Builder.Default
    private String customField2 = "";
    @JsonProperty("CustomField3")
    @Builder.Default
    private String customField3 = "";
    @JsonProperty("CustomField4")
    @Builder.Default
    private String customField4 = "";
    @JsonProperty("CustomField5")
    @Builder.Default
    private String customField5 = "";
    @JsonProperty("CustomField6")
    @Builder.Default
    private String customField6 = "";

    @JsonProperty("CustomList1")
    @Builder.Default
    private String customList1 = "";
    @JsonProperty("CustomList2")
    @Builder.Default
    private String customList2 = "";
    @JsonProperty("CustomList3")
    @Builder.Default
    private String customList3 = "";
    @JsonProperty("CustomList4")
    @Builder.Default
    private String customList4 = "";
    @JsonProperty("CustomList5")
    @Builder.Default
    private String customList5 = "";
    @JsonProperty("CustomList6")
    @Builder.Default
    private String customList6 = "";

    public static Map<String, String> getFieldsMap(CustomFieldsAndLists customFieldsAndLists,
                                                   String prefix, Set<String> fieldList) {

        Map<String, String> fieldsValuesMap = new HashMap<>();
        if (customFieldsAndLists == null) return fieldsValuesMap;

        for (Field field : customFieldsAndLists.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }
            captionValue = prefix + captionValue;
            if (fieldList == null || fieldList.contains(captionValue)) {
                try {
                    Object value = field.get(customFieldsAndLists);
                    if (value != null) {
                        fieldsValuesMap.put(captionValue, value.toString());
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                }
            }
        }
        return fieldsValuesMap;
    }

    public static void setCustomFieldsAndListsValues(
            CustomFieldsAndLists customFieldsAndLists,
            Set<FieldValue> values, String prefix) {
        for (Field field : CustomFieldsAndLists.class.getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }
            captionValue = prefix + captionValue;
            for (FieldValue value : values) {
                if (value.getField().getName().equalsIgnoreCase(captionValue)) {
                    try {
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, customFieldsAndLists, value.getValue());
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                    }
                    break;
                }
            }
        }
    }
}
