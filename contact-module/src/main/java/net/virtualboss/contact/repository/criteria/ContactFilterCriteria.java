package net.virtualboss.contact.repository.criteria;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.entity.FieldValue;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@AllArgsConstructor
@Builder
public class ContactFilterCriteria {
    private static final String COMPANY = "company";

    private String findString;
    private Boolean showUnassigned;
    private Boolean isDeleted;

    public Specification<Contact> getSpecification() {
        return getSpecification(this);
    }

    private Specification<Contact> getSpecification(ContactFilterCriteria criteria) {
        Map<String, Object> fields = new HashMap<>();

        for (Field field : ContactFilterCriteria.class.getDeclaredFields()) {
            try {
                fields.put(field.getName(), field.get(criteria));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to access field: " + field.getName(), e);
            }
        }

        return Specification.allOf(
                fields.keySet().stream()
                        .map(fieldName -> ContactFilterCriteria
                                .getSpecification(fieldName, fields))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private static Specification<Contact> getSpecification(
            String fieldName, Map<String, Object> fields) {
        Object fieldValue = fields.get(fieldName);
        if (fieldValue == null) {
            return null;
        }
        return switch (fieldName) {
            case "findString" -> (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("profession")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("firstName")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lastName")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("notes")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("comments")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("supervisor")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("spouse")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get(COMPANY)), "%" + fieldValue.toString().toLowerCase() + "%"),
                            getCustomFieldsAndListsPredicate(root, cb, fieldValue)
                    );
            case "showUnassigned" -> (root, query, cb) -> getUnassignedContact(root, cb, fieldValue);
            case "VALUE", "CUSTOM_FIELDS_AND_LISTS_VALUES", "COMPANY", "NOTES" ->  null;
            default -> (root, query, cb) -> cb.equal(root.get(fieldName), fieldValue);
        };
    }

private static Predicate getUnassignedContact(Root<Contact> root, CriteriaBuilder cb, Object fieldValue) {
        if ((boolean) fieldValue) {
            return cb.or(cb.equal(cb.lower(root.get(COMPANY)), "unassigned"));
        } else {
            return cb.or(cb.equal(cb.lower(root.get(COMPANY)), "unassigned").not());
        }
}

private static Predicate getCustomFieldsAndListsPredicate(Root<Contact> root, CriteriaBuilder cb, Object fieldValue) {
        Join<Contact, FieldValue> taskFieldValueJoin = root.join("customFieldsAndListsValues", JoinType.LEFT);
        return cb.like(cb.lower(taskFieldValueJoin.get("value")), "%" + fieldValue.toString().toLowerCase() + "%");
    }
}