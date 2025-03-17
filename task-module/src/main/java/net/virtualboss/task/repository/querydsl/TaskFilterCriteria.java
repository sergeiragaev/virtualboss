package net.virtualboss.task.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.common.model.entity.QTask;
import net.virtualboss.common.model.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private List<UUID> excludeTaskIds;

    public BooleanBuilder toPredicate() {
        QTask task = QTask.task;
        BooleanBuilder builder = new BooleanBuilder();

        addDateRangePredicates(task, builder);
        addStatusAndMarkedPredicates(task, builder);
        addListBasedPredicates(task, builder);
        addSearchPredicate(task, builder);
        addDeletionPredicate(task, builder);

        return builder;
    }

    private void addDateRangePredicates(QTask task, BooleanBuilder builder) {
        addDatePredicate(builder, task.targetStart, targetStartFrom, targetStartTo);
        addDatePredicate(builder, task.targetFinish, targetFinishFrom, targetFinishTo);
        addDatePredicate(builder, task.actualFinish, actualFinishFrom, actualFinishTo);
        addAnyDateFieldPredicates(task, builder);
    }

    private void addDatePredicate(BooleanBuilder builder,
                                  DatePath<LocalDate> datePath,
                                  LocalDate from,
                                  LocalDate to) {
        Optional.ofNullable(from).ifPresent(fromDate ->
                builder.and(datePath.goe(fromDate)));
        Optional.ofNullable(to).ifPresent(toDate ->
                builder.and(datePath.loe(toDate)));
    }

    private void addAnyDateFieldPredicates(QTask task, BooleanBuilder builder) {
        if (anyDateFieldFrom != null) {
            builder.and(createAnyDateExpression(task).goe(anyDateFieldFrom));
        }
        if (anyDateFieldTo != null) {
            builder.and(createAnyDateExpression(task).loe(anyDateFieldTo));
        }
    }

    private DateExpression<LocalDate> createAnyDateExpression(QTask task) {
        return task.targetStart.coalesce(task.targetFinish.coalesce(task.actualFinish));
    }

    private void addStatusAndMarkedPredicates(QTask task, BooleanBuilder builder) {
        Optional.ofNullable(status).ifPresent(s ->
                builder.and(task.status.eq(s)));
        Optional.ofNullable(marked).ifPresent(m ->
                builder.and(task.marked.eq(m)));
    }

    private void addListBasedPredicates(QTask task, BooleanBuilder builder) {
        addInPredicate(builder, task.job, jobList);
        addInPredicate(builder, task.contact, contactList);
        addInPredicate(builder, task.id, taskList);
        addNotInPredicate(builder, task.id, excludeTaskIds);
    }

    private <T> void addInPredicate(BooleanBuilder builder,
                                    SimpleExpression<T> path,
                                    List<T> values) {
        if (values != null && !values.isEmpty()) {
            builder.and(path.in(values));
        }
    }

    private <T> void addNotInPredicate(BooleanBuilder builder,
                                       SimpleExpression<T> path,
                                       List<T> values) {
        if (values != null && !values.isEmpty()) {
            builder.and(path.notIn(values));
        }
    }

    private void addSearchPredicate(QTask task, BooleanBuilder builder) {
        if (findString == null || findString.isEmpty()) return;

        String searchStr = findString.toLowerCase();
        BooleanExpression searchPredicate = createSearchPredicate(task, searchStr);
        builder.and(searchPredicate);
    }

    private BooleanExpression createSearchPredicate(QTask task, String searchStr) {
        BooleanExpression taskPredicate = createTaskSearchPredicate(task, searchStr);
        BooleanExpression jobPredicate = createJobSearchPredicate(task, searchStr);
        BooleanExpression contactPredicate = createContactSearchPredicate(task, searchStr);

        return taskPredicate
                .or(jobPredicate)
                .or(contactPredicate);
    }

    private BooleanExpression createTaskSearchPredicate(QTask task, String searchStr) {
        return task.description.containsIgnoreCase(searchStr)
                .or(task.notes.containsIgnoreCase(searchStr))
                .or(task.taskOrder.containsIgnoreCase(searchStr))
                .or(task.customFieldsAndListsValues.any().value.containsIgnoreCase(searchStr));
    }

    private BooleanExpression createJobSearchPredicate(QTask task, String searchStr) {
        return Optional.ofNullable(task.job)
                .map(job -> job.number.containsIgnoreCase(searchStr)
                        .or(job.subdivision.containsIgnoreCase(searchStr))
                        .or(job.lot.containsIgnoreCase(searchStr))
                        .or(job.directions.containsIgnoreCase(searchStr))
                        .or(job.notes.containsIgnoreCase(searchStr))
                        .or(job.ownerName.containsIgnoreCase(searchStr))
                        .or(job.company.containsIgnoreCase(searchStr))
                        .or(job.customFieldsAndListsValues.any().value.containsIgnoreCase(searchStr)))
                .orElse(null);
    }

    private BooleanExpression createContactSearchPredicate(QTask task, String searchStr) {
        return Optional.ofNullable(task.contact)
                .map(contact -> contact.profession.containsIgnoreCase(searchStr)
                        .or(contact.firstName.containsIgnoreCase(searchStr))
                        .or(contact.lastName.containsIgnoreCase(searchStr))
                        .or(contact.notes.containsIgnoreCase(searchStr))
                        .or(contact.comments.containsIgnoreCase(searchStr))
                        .or(contact.supervisor.containsIgnoreCase(searchStr))
                        .or(contact.spouse.containsIgnoreCase(searchStr))
                        .or(contact.company.containsIgnoreCase(searchStr))
                        .or(contact.customFieldsAndListsValues.any().value.containsIgnoreCase(searchStr)))
                .orElse(null);
    }

    private void addDeletionPredicate(QTask task, BooleanBuilder builder) {
        Optional.ofNullable(isDeleted).ifPresent(d ->
                builder.and(task.isDeleted.eq(d)));
    }
}
