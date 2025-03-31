package net.virtualboss.common.mapper.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractResponseMapper<T> {

    private static final ConcurrentHashMap<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>();

    protected Field[] getCachedDeclaredFields(Class<?> clazz) {
        return declaredFieldsCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    protected String getFieldCaption(Field field) {
        return field.isAnnotationPresent(JsonProperty.class)
                ? field.getAnnotation(JsonProperty.class).value()
                : field.getName();
    }

    protected boolean shouldIncludeField(String fieldCaption, Set<String> fieldList) {
        return fieldList == null || fieldList.contains(fieldCaption);
    }

    protected void addFieldToMap(T response, Map<String, Object> responseMap, Field field, String fieldCaption) {
        ReflectionUtils.makeAccessible(field);
        Object value = ReflectionUtils.getField(field, response);
        if (value != null) {
            responseMap.put(fieldCaption, value);
        }
    }

    public Map<String, Object> map(T response, Set<String> fieldList) {
        Map<String, Object> responseMap = createMap();
        Field[] fields = getCachedDeclaredFields(response.getClass());
        for (Field field : fields) {
            String fieldCaption = getFieldCaption(field);
            if (processSpecialField(fieldCaption, response, responseMap, fieldList)) {
                continue;
            }
            if (shouldIncludeField(fieldCaption, fieldList)) {
                addFieldToMap(response, responseMap, field, fieldCaption);
            }
        }
        return responseMap;
    }

    protected Map<String, Object> createMap() {
        return new HashMap<>();
    }

    protected abstract boolean processSpecialField(
            String fieldCaption, T response, Map<String, Object> responseMap, Set<String> fieldList);
}
