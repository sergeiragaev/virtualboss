package net.virtualboss.repository.criteria;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.FieldValue;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.TaskStatus;
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

    private TaskStatus status;
    private Boolean marked;

    private List<Job> jobList;
    private List<Contact> contactList;
    private List<UUID> taskList;

    private String findString;

    private Boolean isDeleted;

    private UUID linkingTask;

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
                                .getSpecification(fieldName, fields, jobList, contactList, taskList))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private static Specification<Task> getSpecification(
            String fieldName, Map<String, Object> fields,
            List<Job> jobList, List<Contact> contactList, List<UUID> taskList) {
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
            case "taskList" -> Specification.anyOf(
                    taskList
                            .stream()
                            .map(TaskFilterCriteria::getSpecification)
                            .toList()
            );
            case "linkingTask" -> (root, query, cb) ->
                    cb.not(
                            cb.or(
                                    cb.equal(root.get("id"), fieldValue)
//                                    , getChildrenSpecificationPredicate(root, query, cb)
                                    )
                    );
            case "findString" -> (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("description")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("notes")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("order")), "%" + fieldValue.toString().toLowerCase() + "%"),
                            getTaskCustomFieldsAndListsPredicate(root, cb, fieldValue),
                            getJobSpecificationPredicate(root, cb, fieldValue),
                            getContactSpecificationPredicate(root, cb, fieldValue)
                    );
            default -> (root, query, cb) -> cb.equal(root.get(fieldName), fieldValue);
        };
    }

    private static Expression<Boolean> getChildrenSpecificationPredicate(
            Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Subquery<UUID> subquery = query.subquery(UUID.class);
        subquery.where(root.join("children").get("id").in(new ArrayList<>()));
        return cb.exists(subquery);
    }

    private static Predicate getContactSpecificationPredicate(Root<Task> root, CriteriaBuilder cb, Object fieldValue) {
        Join<Task, Contact> conactJoin = root.join("contact", JoinType.LEFT);
        Join<Contact, FieldValue> contactFieldValueJoin = conactJoin.join("customFieldsAndListsValues", JoinType.LEFT);
        return cb.or(
                cb.like(cb.lower(conactJoin.get("profession")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("firstName")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("lastName")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("notes")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("comments")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("supervisor")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("spouse")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(conactJoin.get("company")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(contactFieldValueJoin.get("value")), "%" + fieldValue.toString().toLowerCase() + "%")
        );
    }

    private static Predicate getJobSpecificationPredicate(Root<Task> root, CriteriaBuilder cb, Object fieldValue) {
        Join<Task, Job> jobJoin = root.join("job", JoinType.LEFT);
        Join<Job, FieldValue> jobFieldValueJoin = jobJoin.join("customFieldsAndListsValues", JoinType.LEFT);
        return cb.or(
                cb.like(cb.lower(jobJoin.get("number")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobJoin.get("subdivision")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobJoin.get("lot")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobJoin.get("directions")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobJoin.get("notes")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobJoin.get("ownerName")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobJoin.get("company")), "%" + fieldValue.toString().toLowerCase() + "%"),
                cb.like(cb.lower(jobFieldValueJoin.get("value")), "%" + fieldValue.toString().toLowerCase() + "%")
        );
    }

    private static Predicate getTaskCustomFieldsAndListsPredicate(Root<Task> root, CriteriaBuilder cb, Object fieldValue) {
        Join<Task, FieldValue> taskFieldValueJoin = root.join("customFieldsAndListsValues", JoinType.LEFT);
        return cb.like(cb.lower(taskFieldValueJoin.get("value")), "%" + fieldValue.toString().toLowerCase() + "%");
    }

    private static Specification<Task> getSpecification(Object field) {
        if (field instanceof Job) {
            return (root, query, cb) -> cb.equal(root.get("job"), field);
        } else if (field instanceof Contact) {
            return (root, query, cb) -> cb.equal(root.get("contact"), field);
        } else {
            return (root, query, cb) -> cb.equal(root.get("id"), field);
        }
    }
}