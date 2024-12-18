package net.virtualboss.web.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.model.entity.Contact;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@AllArgsConstructor
@Builder
public class ContactFilterCriteria {

    private String findString;
//    private Boolean showUnassigned;

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
                            cb.like(cb.lower(root.get("company")), "%" + fieldValue.toString().toLowerCase() + "%")
                    );
//            case "showUnassigned" -> (root, query, cb) ->
//                    cb.or(
//                            cb.notEqual(cb.lower(root.get("company")), "unassigned"),
//                            cb.equal(root.get("company"), "NULL")
//                    );
            default -> (root, query, cb) -> cb.equal(root.get(fieldName), fieldValue);
        };
    }
}