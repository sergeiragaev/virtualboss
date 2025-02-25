package net.virtualboss.job.repository.criteria;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.common.model.entity.FieldValue;
import net.virtualboss.common.model.entity.Job;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@AllArgsConstructor
@Builder
public class JobFilterCriteria {

    private String findString;

    private Boolean isDeleted;

    public Specification<Job> getSpecification() {
        return getSpecification(this);
    }

    private Specification<Job> getSpecification(JobFilterCriteria criteria) {
        Map<String, Object> fields = new HashMap<>();

        for (Field field : JobFilterCriteria.class.getDeclaredFields()) {
            try {
                fields.put(field.getName(), field.get(criteria));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to access field: " + field.getName(), e);
            }
        }

        return Specification.allOf(
                fields.keySet().stream()
                        .map(fieldName -> JobFilterCriteria
                                .getSpecification(fieldName, fields))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private static Specification<Job> getSpecification(
            String fieldName, Map<String, Object> fields) {
        Object fieldValue = fields.get(fieldName);
        if (fieldValue == null) {
            return null;
        }
        return switch (fieldName) {
            case "findString" -> (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("number")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("subdivision")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lot")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("directions")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("notes")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("ownerName")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("company")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            getCustomFieldsAndListsPredicate(root, cb, fieldValue)
                    );
            default -> (root, query, cb) -> cb.equal(root.get(fieldName), fieldValue);
        };
    }
    private static Predicate getCustomFieldsAndListsPredicate(Root<Job> root, CriteriaBuilder cb, Object fieldValue) {
        Join<Job, FieldValue> taskFieldValueJoin = root.join("customFieldsAndListsValues", JoinType.LEFT);
        return cb.like(cb.lower(taskFieldValueJoin.get("value")), "%" + fieldValue.toString().toLowerCase() + "%");
    }
}