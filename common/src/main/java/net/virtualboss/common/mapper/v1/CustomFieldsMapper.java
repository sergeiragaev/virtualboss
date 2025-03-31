package net.virtualboss.common.mapper.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.virtualboss.common.model.entity.FieldValue;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomFieldsMapper {

    private static final ConcurrentHashMap<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Field, String> fieldNameCache = new ConcurrentHashMap<>();

    private Field[] getCachedDeclaredFields(Class<?> clazz) {
        return declaredFieldsCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    private String getFieldBaseName(Field field) {
        return fieldNameCache.computeIfAbsent(field, f ->
                f.isAnnotationPresent(JsonProperty.class)
                        ? f.getAnnotation(JsonProperty.class).value()
                        : f.getName()
        );
    }

    private String getFieldCaption(Field field, String prefix) {
        return prefix + getFieldBaseName(field);
    }

    public Map<String, String> getFieldsMap(CustomFieldsAndLists instance, String prefix, Set<String> fieldList) {
        Map<String, String> fieldsValuesMap = new HashMap<>();
        if (instance == null) return fieldsValuesMap;

        Field[] fields = getCachedDeclaredFields(instance.getClass());
        for (Field field : fields) {
            String caption = getFieldCaption(field, prefix);
            if (fieldList == null || fieldList.contains(caption)) {
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, instance);
                if (value != null) {
                    fieldsValuesMap.put(caption, value.toString());
                }
            }
        }
        return fieldsValuesMap;
    }

    public void setCustomFieldsAndListsValues(CustomFieldsAndLists instance, Set<FieldValue> values, String prefix) {
        Field[] fields = getCachedDeclaredFields(CustomFieldsAndLists.class);
        for (Field field : fields) {
            String caption = getFieldCaption(field, prefix);
            values.stream()
                    .filter(val -> val.getField().getName().equalsIgnoreCase(caption))
                    .findFirst()
                    .ifPresent(val -> {
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, instance, val.getValue());
                    });
        }
    }
}