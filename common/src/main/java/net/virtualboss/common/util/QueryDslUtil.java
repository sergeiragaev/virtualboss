package net.virtualboss.common.util;

import com.querydsl.core.types.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.dsl.*;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.exception.MappingException;
import net.virtualboss.common.model.entity.QField;
import net.virtualboss.common.model.entity.QFieldValue;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.data.domain.Sort;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryDslUtil {

    private static final Pattern CUSTOM_FIELD_PATTERN = Pattern.compile("(Task|Job|Contact)Custom(.*)");
    private static final Map<String, List<Field>> CUSTOM_FIELDS_CACHE = new HashMap<>();

    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, UUID.class, Boolean.class, Character.class
    );

    private QueryDslUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> Expression<T> buildProjection(Class<T> dtoClass, Object qEntity, List<String> fieldsList) {
        Map<String, Expression<?>> bindings = Arrays.stream(dtoClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EntityMapping.class))
                .flatMap(field -> {
                    Expression<?> expr = processField(field, qEntity, fieldsList);
                    return expr != null
                            ? Stream.of(new AbstractMap.SimpleEntry<>(field.getName(), expr))
                            : Stream.empty();
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return Projections.bean(dtoClass, bindings);
    }

    private static Expression<?> processField(Field dtoField, Object qEntity, List<String> fieldsList) {
        try {

            EntityMapping annotation = dtoField.getAnnotation(EntityMapping.class);
            String entityFieldName = getEntityFieldName(dtoField, annotation);
            String dtoFieldName = getDtoFieldName(dtoField);

            if (isSimpleType(dtoField.getType())) {
                return handleSimpleField(dtoFieldName, qEntity, entityFieldName, fieldsList);
            }

            List<String> nestedFields = extractNestedFields(dtoFieldName, fieldsList);

            if (dtoField.getType().equals(CustomFieldsAndLists.class)) {
                Matcher matcher = CUSTOM_FIELD_PATTERN.matcher(dtoFieldName);
                if (matcher.matches()) {
                    String entityType = matcher.group(1); // Task, Job or Contact
                    return processCustomField(nestedFields, entityType);
                }
            }

            return handleNestedField(dtoField, dtoFieldName, qEntity, entityFieldName, fieldsList);
        } catch (MappingException e) {
            throw e;
        } catch (Exception e) {
            throw new MappingException("Error processing field: " + dtoField.getName());
        }
    }
    
    private static Expression<?> handleSimpleField(String dtoFieldName,
                                                   Object qEntity,
                                                   String entityFieldName,
                                                   List<String> fieldsList) {
        if (!fieldsList.contains(dtoFieldName)) return null;
        return extractQEntityExpression(qEntity, entityFieldName);
    }

    private static Expression<?> handleNestedField(Field dtoField,
                                                   String dtoFieldName,
                                                   Object qEntity,
                                                   String entityFieldName,
                                                   List<String> fieldsList) {

        List<String> nestedFields = extractNestedFields(dtoFieldName, fieldsList);
        if (nestedFields.isEmpty() && !fieldsList.contains(dtoFieldName))
            return null;

        Object nestedQEntity = extractQEntityValue(qEntity, entityFieldName);
        return buildProjection(dtoField.getType(), nestedQEntity, nestedFields);
    }

    private static String getEntityFieldName(Field dtoField, EntityMapping annotation) {
        return annotation.path().isEmpty() ? dtoField.getName() : annotation.path();
    }

    private static String getDtoFieldName(Field dtoField) {
        return dtoField.isAnnotationPresent(JsonProperty.class)
                ? dtoField.getAnnotation(JsonProperty.class).value()
                : dtoField.getName();
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isEnum() ||
               type.isPrimitive() ||
               SIMPLE_TYPES.contains(type) ||
               Number.class.isAssignableFrom(type) ||
               java.util.Date.class.isAssignableFrom(type) ||
               java.time.temporal.Temporal.class.isAssignableFrom(type);
    }

    private static Expression<?> extractQEntityExpression(Object qEntity, String fieldName) {
        try {
            Field qField = qEntity.getClass().getDeclaredField(fieldName);
            ReflectionUtils.makeAccessible(qField);
            return (Expression<?>) qField.get(qEntity);
        } catch (Exception e) {
            throw createMappingException(fieldName, qEntity.getClass());
        }
    }

    private static MappingException createMappingException(String fieldName, Class<?> qEntityClass) {
        return new MappingException(
                MessageFormat.format("Field {0} not found in {1}", fieldName, qEntityClass.getName())
        );
    }

    private static List<String> extractNestedFields(String prefix, List<String> fieldsList) {
        return fieldsList.stream()
                .filter(f -> f.startsWith(prefix + "."))
                .map(f -> f.substring(prefix.length() + 1))
                .toList();
    }

    private static Object extractQEntityValue(Object qEntity, String fieldName) {
        try {
            Field qField = qEntity.getClass().getDeclaredField(fieldName);
            ReflectionUtils.makeAccessible(qField);
            return qField.get(qEntity);
        } catch (Exception e) {
            throw createMappingException(fieldName, qEntity.getClass());
        }
    }

    /**
     * Converts a sort (Spring Data) to an array of OrderSpecifier (QueryDSL).
     *
     * @param sort  the Spring Data sort object
     * @param alias the alias for the entity (e.g. "task")
     * @param <T>   the entity type
     * @return an array of OrderSpecifier for use in QueryDSL.orderBy(...)
     */
    @SuppressWarnings("unchecked")
    public static <T> OrderSpecifier<Comparable<Object>>[] getOrderSpecifiers(Sort sort, Class<T> entityClass, String alias) {
        List<OrderSpecifier<Comparable<Object>>> orders = new ArrayList<>();
        // Use PathBuilder to dynamically build paths
        PathBuilder<T> entityPath = new PathBuilder<>(entityClass, alias);

        sort.forEach(order -> {
            Order querydslOrder = order.isAscending() ? Order.ASC : Order.DESC;
            // Use getComparable to get an Expression that is compatible with the OrderSpecifier
            OrderSpecifier<?> orderSpecifier = new OrderSpecifier<>(
                    querydslOrder,
                    entityPath.getComparable(order.getProperty(), Comparable.class)
            );
            orders.add((OrderSpecifier<Comparable<Object>>) orderSpecifier);
        });

        return orders.toArray(new OrderSpecifier[0]);
    }

    private static Expression<?> processCustomField(List<String> nestedFields, String entityType) {
        if (nestedFields.isEmpty()) return null;

        QFieldValue fieldValue = new QFieldValue("fieldValue" + entityType);
        QField field = new QField("field" + entityType);
        Map<String, Expression<?>> bindings = new HashMap<>();

        getCustomFields().forEach(f -> {
            String jsonName = entityType + f.getAnnotation(JsonProperty.class).value();
            if (nestedFields.contains(jsonName)) {

                Expression<String> expr = Expressions.cases()
                        .when(field.name.eq(jsonName))
                        .then(fieldValue.value)
                        .otherwise("")
                        .max();

                bindings.put(f.getName(), expr);
            }
        });

        return bindings.isEmpty() ? null : Projections.bean(CustomFieldsAndLists.class, bindings);
    }

    private static List<Field> getCustomFields() {
        return CUSTOM_FIELDS_CACHE.computeIfAbsent("fields", k ->
                Arrays.stream(CustomFieldsAndLists.class.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(JsonProperty.class))
                        .toList()
        );
    }
}