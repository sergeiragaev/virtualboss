package net.virtualboss.web.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.entity.Task;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

@Getter
@AllArgsConstructor
@Builder
public class TaskFilterCriteria {
    private LocalDate targetStartFrom;
    private LocalDate targetStartTo;
    private LocalDate targetFinishFrom;
    private LocalDate targetFinishTo;
    private LocalDate actualFinishFrom;
    private LocalDate actualFinishTo;

    private LocalDate anyDateFieldFrom;
    private LocalDate anyDateFieldTo;

    private String status;
    private Boolean marked;

    private List<Job> jobList;
    private List<Contact> contactList;

    private String findString;

    public Specification<Task> getSpecification() {
        return getSpecification(this);
    }

    private Specification<Task> getSpecification(TaskFilterCriteria criteria) {
        Map<String, Object> fields = new HashMap<>();

        for (Field field : TaskFilterCriteria.class.getDeclaredFields()) {
            try {
                fields.put(field.getName(), field.get(criteria));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to access field: " + field.getName(), e);
            }
        }

        return Specification.allOf(
                fields.keySet().stream()
                        .map(fieldName -> TaskFilterCriteria
                                .getSpecification(fieldName, fields, jobList, contactList))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private static Specification<Task> getSpecification(
            String fieldName, Map<String, Object> fields,
            List<Job> jobList, List<Contact> contactList) {
        final String targetStart = "targetStart";
        final String targetFinish = "targetFinish";
        final String actualFinish = "actualFinish";

        Object fieldValue = fields.get(fieldName);
        if (fieldValue == null) {
            return null;
        }
        return switch (fieldName) {
            case "targetStartTo" -> (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get(targetStart), (LocalDate) fieldValue);
            case "targetStartFrom" -> (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get(targetStart), (LocalDate) fieldValue);
            case "targetFinishTo" -> (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get(targetFinish), (LocalDate) fieldValue);
            case "targetFinishFrom" -> (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get(targetFinish), (LocalDate) fieldValue);
            case "actualFinishTo" -> (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get(actualFinish), (LocalDate) fieldValue);
            case "actualFinishFrom" -> (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get(actualFinish), (LocalDate) fieldValue);
            case "anyDateFieldFrom" -> (root, query, cb) ->
                    cb.or(
                            cb.greaterThanOrEqualTo(root.get(targetStart), (LocalDate) fieldValue),
                            cb.greaterThanOrEqualTo(root.get(targetFinish), (LocalDate) fieldValue),
                            cb.greaterThanOrEqualTo(root.get(actualFinish), (LocalDate) fieldValue)
                    );
            case "anyDateFieldTo" -> (root, query, cb) ->
                    cb.or(
                            cb.lessThanOrEqualTo(root.get(targetStart), (LocalDate) fieldValue),
                            cb.lessThanOrEqualTo(root.get(targetFinish), (LocalDate) fieldValue),
                            cb.lessThanOrEqualTo(root.get(actualFinish), (LocalDate) fieldValue)
                    );
            case "jobList" -> Specification.anyOf(
                    jobList
                            .stream()
                            .map(TaskFilterCriteria::getSpecification)
                            .toList()
            );
            case "contactList" -> Specification.anyOf(
                    contactList
                            .stream()
                            .map(TaskFilterCriteria::getSpecification)
                            .toList()
            );
            case "findString" -> (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("description")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("notes")), "%" + fieldValue.toString().toLowerCase() + "%")
                    );
            default -> (root, query, cb) -> cb.equal(root.get(fieldName), fieldValue);
        };
    }

    private static Specification<Task> getSpecification(Object field) {
        if (field instanceof Job) {
            return (root, query, cb) -> cb.equal(root.get("job"), field);
        } else {
            return (root, query, cb) -> cb.equal(root.get("contact"), field);
        }
    }

}