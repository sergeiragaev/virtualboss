package net.virtualboss.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.virtualboss.common.annotation.Flatten;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DtoFlattener {

    private DtoFlattener() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, Object> flatten(Object dto, List<String> fieldsList) {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flattenInternal("", dto, flatMap, fieldsList);
        return flatMap;
    }

    private static void flattenInternal(String prefix, Object obj, Map<String, Object> result, List<String> fieldsList) {
        if (obj == null) return;

        for (Field field : obj.getClass().getDeclaredFields()) {
            ReflectionUtils.makeAccessible(field);
            try {
                Object value = field.get(obj);
                if (value == null) continue;

                Flatten flattenAnnotation = field.getAnnotation(Flatten.class);
                if (flattenAnnotation != null) {
                    String newPrefix = prefix + flattenAnnotation.prefix();
                    flattenInternal(newPrefix, value, result, fieldsList);
                } else {
                    String key = prefix + getJsonFieldName(field);
                    if (fieldsList.contains(key)) result.put(key, value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to access field: " + field.getName(), e);
            }
        }
    }

    private static String getJsonFieldName(Field field) {
        JsonProperty jsonProp = field.getAnnotation(JsonProperty.class);
        return jsonProp != null ? jsonProp.value() : field.getName();
    }
}