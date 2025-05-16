package net.virtualboss.common.util;

import com.querydsl.core.types.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.exception.MappingException;
import net.virtualboss.common.model.entity.*;
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

import static net.virtualboss.common.model.entity.QJob.job;
import static net.virtualboss.common.model.entity.QTask.task;

public class QueryDslUtil {

    private static final Pattern CUSTOM_FIELD_PATTERN = Pattern.compile("(Task|Job|Contact)Custom(.*)");
    private static final Map<String, List<Field>> CUSTOM_FIELDS_CACHE = new HashMap<>();
    private static final String CUSTOM_FIELDS_PREFIX = "customFields";
    private static final String TASK_FOLLOWS_FIELD = "follows";
    private static final String TASK_FOLLOWS_ALIAS = "task_follows_0";
    private static final String CONTACT_PERSON_FIELD = "person";
    private static final String NAME_CONCAT_TEMPLATE = "CONCAT({0}, ' ', {1})";
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, UUID.class, Boolean.class, Character.class
    );
    private static final String FIELD_VALUE = "fieldValue";
    private static final String FIELD = "field";
    private static final String ADDRESSES = "addresses";
    private static final String PHONES = "phones";

    private QueryDslUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> Expression<T> buildProjection(Class<T> dtoClass, Object qEntity, List<String> fieldsList) {
        Map<String, Expression<?>> bindings = Arrays.stream(dtoClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EntityMapping.class))
                .flatMap(field -> processFieldToExpression(field, qEntity, fieldsList))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return Projections.bean(dtoClass, bindings);
    }

    private static Stream<Map.Entry<String, Expression<?>>> processFieldToExpression(
            Field dtoField,
            Object qEntity,
            List<String> fieldsList
    ) {
        try {
            Expression<?> expr = processField(dtoField, qEntity, fieldsList);
            return expr != null
                    ? Stream.of(new AbstractMap.SimpleEntry<>(dtoField.getName(), expr))
                    : Stream.empty();
        } catch (MappingException e) {
            throw e;
        } catch (Exception e) {
            throw new MappingException("Error processing field: " + dtoField.getName(), e);
        }
    }

    private static Expression<?> processField(Field dtoField, Object qEntity, List<String> fieldsList) {
        EntityMapping annotation = dtoField.getAnnotation(EntityMapping.class);
        String entityFieldName = getEntityFieldName(dtoField, annotation);
        String dtoFieldName = getDtoFieldName(dtoField);

        if (TASK_FOLLOWS_FIELD.equals(entityFieldName)) {
            return createTaskFollowsExpression();
        }

        if (CONTACT_PERSON_FIELD.equals(entityFieldName) && fieldsList.contains(dtoFieldName)) {
            return createContactPersonExpression(qEntity);
        }

        if ("statusColor".equals(entityFieldName)) {
            return createTaskStatusColorExpression();
        }

        if (ADDRESSES.equals(entityFieldName) && fieldsList.contains(dtoFieldName)) {
            return createAddressesExpression(qEntity, false);
        }

        if (PHONES.equals(entityFieldName) && fieldsList.contains(dtoFieldName)) {
            return createPhonesExpression(qEntity, false);
        }

        if (isSimpleType(dtoField.getType())) {
            return handleSimpleTypeField(dtoFieldName, qEntity, entityFieldName, fieldsList);
        }

        if (dtoField.getType().equals(CustomFieldsAndLists.class)) {
            return handleCustomFields(dtoFieldName, extractNestedFields(dtoFieldName, fieldsList));
        }

        return handleComplexTypeField(dtoField, dtoFieldName, qEntity, entityFieldName, fieldsList);
    }

    private static Expression<String> createTaskStatusColorExpression() {
        QTaskStatusColor taskStatusColor = QTaskStatusColor.taskStatusColor;

        return Expressions.stringTemplate(
                "COALESCE({0}, '#FFFFFF')",
                taskStatusColor.color
        );
    }

    private static Expression<?> handleSimpleTypeField(
            String dtoFieldName,
            Object qEntity,
            String entityFieldName,
            List<String> fieldsList
    ) {
        return fieldsList.contains(dtoFieldName)
                ? getQEntityExpression(qEntity, entityFieldName)
                : null;
    }

    private static Expression<?> handleComplexTypeField(
            Field dtoField,
            String dtoFieldName,
            Object qEntity,
            String entityFieldName,
            List<String> fieldsList
    ) {
        List<String> nestedFields = extractNestedFields(dtoFieldName, fieldsList);
        if (nestedFields.isEmpty() && !fieldsList.contains(dtoFieldName)) return null;

        Object nestedQEntity = getQEntityValue(qEntity, entityFieldName);
        return buildProjection(dtoField.getType(), nestedQEntity, nestedFields);
    }

    private static Expression<?> handleCustomFields(String dtoFieldName, List<String> nestedFields) {
        Matcher matcher = CUSTOM_FIELD_PATTERN.matcher(dtoFieldName);
        if (matcher.matches()) {
            String entityType = matcher.group(1);
            return createCustomFieldProjection(nestedFields, entityType);
        }
        return null;
    }

    private static Expression<?> createCustomFieldProjection(List<String> nestedFields, String entityType) {
        if (nestedFields.isEmpty()) return null;

        QFieldValue fieldValue = new QFieldValue(FIELD_VALUE + entityType);
        QField field = new QField(FIELD + entityType);
        Map<String, Expression<?>> bindings = new HashMap<>();

        getCustomFields().forEach(f -> {
            String jsonName = entityType + f.getAnnotation(JsonProperty.class).value();
            if (nestedFields.contains(jsonName)) {
                bindings.put(f.getName(), createFieldValueExpression(field, fieldValue, jsonName));
            }
        });

        return bindings.isEmpty() ? null : Projections.bean(CustomFieldsAndLists.class, bindings);
    }

    private static Expression<String> createFieldValueExpression(QField field, QFieldValue fieldValue, String jsonName) {
        return Expressions.cases()
                .when(field.name.eq(jsonName))
                .then(fieldValue.customValue)
                .otherwise("")
                .max();
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
               Date.class.isAssignableFrom(type) ||
               java.time.temporal.Temporal.class.isAssignableFrom(type);
    }

    private static Expression<?> getQEntityExpression(Object qEntity, String fieldName) {
        try {
            Field qField = qEntity.getClass().getDeclaredField(fieldName);
            ReflectionUtils.makeAccessible(qField);
            return (Expression<?>) qField.get(qEntity);
        } catch (Exception e) {
            throw createMappingException(fieldName, qEntity.getClass(), e);
        }
    }

    private static Object getQEntityValue(Object qEntity, String fieldName) {
        try {
            Field qField = qEntity.getClass().getDeclaredField(fieldName);
            ReflectionUtils.makeAccessible(qField);
            return qField.get(qEntity);
        } catch (Exception e) {
            throw createMappingException(fieldName, qEntity.getClass(), e);
        }
    }

    private static MappingException createMappingException(String fieldName, Class<?> qEntityClass, Exception cause) {
        String message = MessageFormat.format("Field {0} not found in {1}", fieldName, qEntityClass.getName());
        return new MappingException(message, cause);
    }

    private static List<String> extractNestedFields(String prefix, List<String> fieldsList) {
        return fieldsList.stream()
                .filter(f -> f.startsWith(prefix + "."))
                .map(f -> f.substring(prefix.length() + 1))
                .toList();
    }

    public static <T> OrderSpecifier<Comparable<Object>>[] getOrderSpecifiers(
            Sort sort,
            Class<T> entityClass,
            String alias
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        PathBuilder<T> entityPath = new PathBuilder<>(entityClass, alias);

        sort.forEach(order -> processSortOrder(order, entityPath, orders));

        return orders.toArray(OrderSpecifier[]::new);
    }

    private static void processSortOrder(
            Sort.Order order,
            PathBuilder<?> entityPath,
            List<OrderSpecifier<?>> orders
    ) {
        String property = order.getProperty();
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        Expression<?> expr = switch (property) {
            case TASK_FOLLOWS_FIELD -> createTaskFollowsExpression();
            case "contact.person" -> createContactPersonExpression(task.contact);
            case "owner.person" -> createContactPersonExpression(job.owner);
            case CONTACT_PERSON_FIELD -> createContactPersonExpression(QContact.contact);
            case "taskOrder" -> createTaskOrderExpression();
            case ADDRESSES -> createAddressesExpression(QContact.contact, true);
            case "contact.addresses" -> createAddressesExpression(task.contact, true);
            case "owner.addresses" -> createAddressesExpression(job.owner, true);
            case PHONES -> createPhonesExpression(QContact.contact, true);
            case "contact.phones" -> createPhonesExpression(task.contact, true);
            case "owner.phones" -> createPhonesExpression(job.owner, true);
            default -> isCustomFieldPath(property)
                    ? handleCustomFieldSort(property, entityPath)
                    : entityPath.getComparable(property, Comparable.class);
        };

        orders.add(new OrderSpecifier<>(direction, (Expression<Comparable<Object>>) expr));
    }

    private static Expression<?> createTaskOrderExpression() {
        try {
            Expression<?> taskOrder = getQEntityExpression(task, "taskOrder");
            return Expressions.stringTemplate("try_cast({0}, 0)", taskOrder);
        } catch (Exception e) {
            throw new MappingException("Error creating order expression", e);
        }
    }

    private static Expression<?> createTaskFollowsExpression() {
        QTask taskFollows = new QTask(TASK_FOLLOWS_ALIAS);
        Path<?> valuePath = Expressions.path(String.class, taskFollows, "number");
        return Expressions.stringTemplate("string_agg(cast({0} as text), ',')", valuePath);
    }

    private static Expression<String> createAddressesExpression(Object qEntity, boolean forSorting) {
        if (forSorting) return Expressions.stringPath(ADDRESSES);

        QAddress address = QAddress.address;
        QCommunicationType type = QCommunicationType.communicationType;

        return ExpressionUtils.as(
                JPAExpressions.select(
                        Expressions.stringTemplate(
                                "string_agg( cast(coalesce(CONCAT_WS(', ', " +
                                "CONCAT({0}, ': ', {1}, " +
                                "CASE WHEN {2} IS NOT NULL AND {2} <> '' THEN CONCAT(', ', {2}) ELSE '' END), " +
                                "{3}, {4}, {5}), '') as text), ';')",
                                type.caption,
                                address.address1,
                                address.address2,
                                address.city,
                                address.state,
                                address.postal
                        )
                )
                .from(address)
                .leftJoin(address.type, type)
                .where(address.address1.isNotEmpty())
                .where(address.entityId.eq(((QContact) qEntity).id))
                .groupBy(address.entityId),
                ADDRESSES
        );
    }

    private static Expression<?> createPhonesExpression(Object qEntity, boolean forSorting) {
        if (forSorting) return Expressions.stringPath(PHONES);

        QCommunication comm = QCommunication.communication;
        QCommunicationType type = QCommunicationType.communicationType;

        return ExpressionUtils.as(
                JPAExpressions.select(
                        Expressions.stringTemplate(
                                "string_agg(cast(coalesce(" +
                                "case when {0} is not null and {0} <> '' then {1} || ': ' || {0} " +
                                "else null end, '') as text), ',')",
                                comm.title,
                                type.caption)
                )
                .from(comm)
                .leftJoin(comm.type, type)
                .where(comm.entityId.eq(((QContact) qEntity).id))
                .groupBy(comm.entityId),
                PHONES
        );
    }

    public static Expression<String> safeOrderBy(Expression<?> expr) {
        return Expressions.stringTemplate("({0})", expr); // предотвращает склейку by+имя
    }

    private static Expression<String> createContactPersonExpression(Object qEntity) {
        try {
            Expression<?> firstName = getQEntityExpression(qEntity, "firstName");
            Expression<?> lastName = getQEntityExpression(qEntity, "lastName");
            return Expressions.stringTemplate(NAME_CONCAT_TEMPLATE, firstName, lastName);
        } catch (Exception e) {
            throw new MappingException("Error creating contact person expression", e);
        }
    }

    private static boolean isCustomFieldPath(String property) {
        return property.contains(CUSTOM_FIELDS_PREFIX);
    }

    private static Expression<?> handleCustomFieldSort(String property, PathBuilder<?> entityPath) {
        String[] parts = property.split("\\.");

        if (parts.length >= 3 && CUSTOM_FIELDS_PREFIX.equals(parts[1])) {
            String nestedEntity = parts[0];
            String fieldName = parts[2];
            return createCustomFieldExpression(nestedEntity, fieldName);
        }

        if (parts.length >= 2 && CUSTOM_FIELDS_PREFIX.equals(parts[0])) {
            String fieldName = parts[1];
            String entityType = entityPath.getType().getSimpleName();
            return createCustomFieldExpression(entityType, fieldName);
        }

        throw new IllegalArgumentException("Invalid custom field path: " + property);
    }

    private static Expression<?> createCustomFieldExpression(String entityPrefix, String customFieldName) {
        entityPrefix = capitalize(entityPrefix);
        QFieldValue fieldValue = new QFieldValue(FIELD_VALUE + entityPrefix);
        QField field = new QField(FIELD + entityPrefix);
        return createFieldValueExpression(field, fieldValue, entityPrefix + customFieldName);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static List<Field> getCustomFields() {
        return CUSTOM_FIELDS_CACHE.computeIfAbsent("fields", k ->
                Arrays.stream(CustomFieldsAndLists.class.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(JsonProperty.class))
                        .toList()
        );
    }
}