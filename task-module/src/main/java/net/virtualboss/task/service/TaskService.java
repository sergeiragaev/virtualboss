package net.virtualboss.task.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.CircularLinkingException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.model.enums.*;
import net.virtualboss.common.util.*;
import net.virtualboss.task.mapper.v1.TaskMapperV1;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.TaskResponse;
import net.virtualboss.task.web.dto.TaskFilter;
import net.virtualboss.common.repository.ContactRepository;
import net.virtualboss.common.repository.JobRepository;
import net.virtualboss.common.repository.TaskRepository;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class TaskService {
    private final TaskRepository taskRepository;
    private final JobRepository jobRepository;
    private final ContactRepository contactRepository;
    private final TaskMapperV1 taskMapper;
    private final TaskScheduleService taskScheduleService;
    private final WorkingDaysCalculator workingDaysCalculator;

    @PersistenceContext
    private final EntityManager entityManager;

    @Cacheable(value = "task", key = "#id")
    public Map<String, Object> findById(String id) {
        Task task = getTaskById(id);
        return TaskResponse.getFieldsMap(taskMapper.taskToResponse(task), null);
    }

    public Task getTaskById(String id) {
        return taskRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Task with id: {0} not found!", id)));
    }

    public List<Map<String, Object>> findAll(String fields, TaskFilter filter) {
        if (fields == null) fields = "TaskId,TaskDescription";
        Set<String> fieldsSet = parseFields(fields);
        List<String> fieldsList = List.copyOf(fieldsSet);

        QTask task = QTask.task;
        QJob job = QJob.job;
        QContact contact = QContact.contact;

        QFieldValue fieldValueTask = new QFieldValue("fieldValueTask");
        QField fieldTask = new QField("fieldTask");

        QFieldValue fieldValueJob = new QFieldValue("fieldValueJob");
        QField fieldJob = new QField("fieldJob");

        QFieldValue fieldValueContact = new QFieldValue("fieldValueContact");
        QField fieldContact = new QField("fieldContact");

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        initializeFilterDefaults(filter);
        PageRequest pageRequest = createPageRequest(filter);
        OrderSpecifier<?>[] orderSpecifiers =
                QueryDslUtil.getOrderSpecifiers(pageRequest.getSort(), Task.class, "task");

        List<TaskResponse> tasks = queryFactory.select(
                        QueryDslUtil.buildProjection(TaskResponse.class, task, fieldsList)
                )
                .from(task)
                .leftJoin(task.job, job)
                .leftJoin(task.contact, contact)
                .leftJoin(task.customFieldsAndListsValues, fieldValueTask)
                .leftJoin(job.customFieldsAndListsValues, fieldValueJob)
                .leftJoin(contact.customFieldsAndListsValues, fieldValueContact)
                .leftJoin(fieldValueTask.field, fieldTask)
                .leftJoin(fieldValueJob.field, fieldJob)
                .leftJoin(fieldValueContact.field, fieldContact)
                .where(
                        buildTaskFilterCriteriaQuery(filter)
                )
                .orderBy(orderSpecifiers)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .groupBy(task.id).groupBy(task.contact).groupBy(task.job)
                .fetch();

        return tasks.stream().map(DtoFlattener::flatten).toList();
    }

    private Set<String> parseFields(String fields) {
        Set<String> fieldsSet = Arrays.stream(fields.split(","))
                .map(string -> {
                    if (string.contains("TaskCustom")) return "TaskCustomFieldsAndLists." + string;
                    if (string.contains("JobCustom")) return "job.JobCustomFieldsAndLists." + string;
                    if (string.contains("ContactCustom")) return "contact.ContactCustomFieldsAndLists." + string;
                    if (string.startsWith("Job")) return "job." + string;
                    if (string.startsWith("Contact")) return "contact." + string;
                    return string;
                })
                .collect(Collectors.toSet());
        fieldsSet.addAll(List.of("TaskId", "job.JobId", "contact.ContactId"));
        return fieldsSet;
    }

    private void initializeFilterDefaults(TaskFilter filter) {
        if (filter.getSize() == null) filter.setSize(100);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("number asc");
    }

    private PageRequest createPageRequest(TaskFilter filter) {
        return PageRequest.of(
                filter.getPage() - 1,
                filter.getSize(),
                Sort.by(parseSortOrders(filter.getSort()))
        );
    }

    private List<Sort.Order> parseSortOrders(String sortString) {
        return Arrays.stream(sortString.split(","))
                .map(this::createSortOrder)
                .toList();
    }

    private Sort.Order createSortOrder(String sort) {
        String[] parts = sort.trim().split(" ");
        Sort.Direction direction = Sort.Direction.valueOf(parts[1].toUpperCase());
        return new Sort.Order(direction, parts[0]);
    }

    private TaskStatus determineTaskStatus(TaskFilter filter) {
        boolean isActive = Boolean.TRUE.equals(filter.getIsActive());
        boolean isDone = Boolean.TRUE.equals(filter.getIsDone());

        if (isDone && !isActive) return TaskStatus.DONE;
        if (isActive && !isDone) return TaskStatus.ACTIVE;
        return null;
    }

    private BooleanBuilder buildTaskFilterCriteriaQuery(TaskFilter filter) {
        return net.virtualboss.task.repository.querydsl.TaskFilterCriteria.builder()
                .findString(StringUtils.isBlank(filter.getFindString()) ? null : filter.getFindString())
                .status(determineTaskStatus(filter))
                .marked(filter.getIsMarked())
                .isDeleted(filter.getIsDeleted())
                .targetStartFrom(getFilterDate(filter, "targetStartFrom"))
                .targetStartTo(getFilterDate(filter, "targetStartTo"))
                .targetFinishFrom(getFilterDate(filter, "targetFinishFrom"))
                .targetFinishTo(getFilterDate(filter, "targetFinishTo"))
                .actualFinishFrom(getFilterDate(filter, "actualFinishFrom"))
                .actualFinishTo(getFilterDate(filter, "actualFinishTo"))
                .anyDateFieldFrom(getFilterDate(filter, "anyDateFieldFrom"))
                .anyDateFieldTo(getFilterDate(filter, "anyDateFieldTo"))
                .jobList(getJobsByIds(filter.getJobIds()))
                .contactList(getContactsByIds(filter.getContactIds()))
                .taskList(getTaskIdsByNumbers(filter.getTaskIds()))
                .excludeTaskIds(getExcludedTaskIds(filter))
                .build().toPredicate();
    }

    private List<UUID> getExcludedTaskIds(TaskFilter filter) {
        if (filter.getLinkingTask() == null) return List.of();

        UUID linkingTaskId = UUID.fromString(filter.getLinkingTask());
        List<UUID> excludedIds = new ArrayList<>(taskRepository.findAllPendingIdsRecursive(linkingTaskId));
        excludedIds.add(linkingTaskId);
        return excludedIds;
    }

    private List<Job> getJobsByIds(List<String> jobIds) {
        return jobIds != null ?
                jobRepository.findAllById(convertToUUIDList(jobIds)) :
                null;
    }

    private List<Contact> getContactsByIds(List<String> contactIds) {
        return contactIds != null ?
                contactRepository.findAllById(convertToUUIDList(contactIds)) :
                null;
    }

    private List<UUID> getTaskIdsByNumbers(List<String> taskNumbers) {
        if (taskNumbers == null) return List.of();
        return taskRepository.findAllByNumberIn(
                        taskNumbers.stream()
                                .map(Long::valueOf)
                                .toList()
                ).stream()
                .map(Task::getId)
                .toList();
    }

    private List<UUID> convertToUUIDList(List<String> ids) {
        return ids.stream()
                .map(UUID::fromString)
                .toList();
    }

    private LocalDate getFilterDate(TaskFilter filter, String dateField) {
        Map<String, LocalDate> dates = setFilterDates(filter);
        return dates.get(dateField);
    }

    private Map<String, LocalDate> setFilterDates(TaskFilter filter) {
        Map<String, LocalDate> filterDates = new HashMap<>();

        if (filter.getIsDateRange() == null) {
            return filterDates;
        }

        DateType dateType = DateType.fromValue(filter.getDateType());
        DateRange dateRange = DateRange.fromValue(filter.getDateRange());
        DateCriteria dateCriteria = DateCriteria.fromValue(filter.getDateCriteria());

        if (dateRange == DateRange.TODAY || dateRange == DateRange.EXACT_DATE) {
            adjustDateRangeForSpecialCases(filter);
        }

        String dateFieldPrefix = getDateFieldPrefix(dateType);
        applyDateCriteria(filter, dateCriteria, dateFieldPrefix, filterDates);

        return filterDates;
    }

    private String getDateFieldPrefix(DateType dateType) {
        return switch (dateType) {
            case TARGET_START -> "targetStart";
            case TARGET_FINISH -> "targetFinish";
            case ACTUAL_FINISH -> "actualFinish";
            case ANY_DATE_FIELD -> "anyDateField";
        };
    }

    private void adjustDateRangeForSpecialCases(TaskFilter filter) {
        LocalDate referenceDate = filter.getThisDate() == null ? LocalDate.now() : filter.getThisDate();

        Integer dateCriteriaValue = filter.getDateCriteria();
        DateCriteria dateCriteria = dateCriteriaValue != null
                ? DateCriteria.fromValue(dateCriteriaValue)
                : DateCriteria.EXACT; // default value

        if (dateCriteria == DateCriteria.ON_OR_BEFORE) {
            filter.setDateTo(referenceDate);
        } else if (dateCriteria == DateCriteria.ON_OR_AFTER) {
            filter.setDateFrom(referenceDate);
        } else {
            filter.setDateFrom(referenceDate);
            filter.setDateTo(referenceDate);
        }
    }

    private void applyDateCriteria(TaskFilter filter,
                                   DateCriteria criteria,
                                   String fieldPrefix,
                                   Map<String, LocalDate> result) {
        if (criteria == DateCriteria.ON_OR_BEFORE) {
            result.put(fieldPrefix + "To", filter.getDateTo());
        } else if (criteria == DateCriteria.ON_OR_AFTER) {
            result.put(fieldPrefix + "From", filter.getDateFrom());
        } else if (criteria == DateCriteria.EXACT) {
            result.put(fieldPrefix + "From", filter.getDateFrom());
            result.put(fieldPrefix + "To", filter.getDateTo());
        }
    }

    @Transactional
    @CachePut(value = "task", key = "#id")
    public Map<String, Object> saveTask(
            String id,
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists,
            TaskReferencesRequest referenceRequest) {
        Task task = taskMapper.requestToTask(id, request, customFieldsAndLists, referenceRequest);
        Task taskFromDb = getTaskById(id);
        checkIfFollowsAlreadyPending(task, taskFromDb);
        task.getCustomFieldsAndListsValues().addAll(taskFromDb.getCustomFieldsAndListsValues());
        removeTasksFromJobAndContact(taskFromDb);
        BeanUtils.copyNonNullProperties(task, taskFromDb);
        taskFromDb.setJob(task.getJob());
        taskFromDb.assignTasksToJobAndContact();
        taskScheduleService.recalculateTaskDates(taskFromDb);
        return TaskResponse.getFieldsMap(taskMapper.taskToResponse(taskFromDb), null);
    }

    @Transactional
    @CacheEvict(value = "task", allEntries = true)
    public List<Map<String, Object>> updateTaskByStartAndFinish(
            String id,
            LocalDate targetStart,
            LocalDate targetFinish) {
        Task taskFromDb = getTaskById(id);
        if (targetStart != null) {
            targetStart = workingDaysCalculator.addWorkDays(targetStart, 0, "US");
            int workingDays = (int) workingDaysCalculator.countBusinessDays(
                    taskFromDb.getTargetStart(),
                    targetStart,
                    "US"
            );
            if (!taskFromDb.getFollows().isEmpty()) {
                taskFromDb.setFinishPlus(taskFromDb.getFinishPlus() + workingDays);
            }
            if (targetFinish == null) {
                taskFromDb.setDuration(taskFromDb.getDuration() - workingDays);
            }
            taskFromDb.setTargetStart(targetStart);
        } else {
            int workingDays = (int) workingDaysCalculator.countBusinessDays(
                    taskFromDb.getTargetFinish(),
                    targetFinish,
                    "US"
            );
            taskFromDb.setDuration(taskFromDb.getDuration() + workingDays);
        }
        taskScheduleService.recalculateTaskDates(taskFromDb);
        TaskFilter filter = new TaskFilter();
        List<String> taskNumbers = new ArrayList<>(taskRepository.findAllById(
                        taskRepository.findAllPendingIdsRecursive(taskFromDb.getId()))
                .stream().map(Task::getNumber).map(String::valueOf).toList());
        taskNumbers.add(taskFromDb.getNumber().toString());
        filter.setTaskIds(taskNumbers);
        return findAll("TaskId,TaskTargetStart,TaskTargetFinish,TaskDuration", filter);
    }

    @Transactional
    @CacheEvict(value = "task", allEntries = true)
    public Map<String, Object> createNewTask(
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists,
            TaskReferencesRequest referenceRequest) {
        Task task = taskMapper.requestToTask(request, customFieldsAndLists, referenceRequest);
        task.setNumber(getNextTaskNumberSequenceValue());
        LocalDate validTargetStart =
                workingDaysCalculator.addWorkDays(
                        request.getTargetStart().minusDays(1), 1, "US");
        task.setTargetStart(validTargetStart);
        task.setTargetFinish(
                workingDaysCalculator.addWorkDays(validTargetStart, task.getDuration() - 1, "US"));
        entityManager.persist(task);
        return TaskResponse.getFieldsMap(taskMapper.taskToResponse(
                taskRepository.save(task)), null);
    }

    @Transactional
    @CacheEvict(value = "task", key = "#id")
    public void deleteTaskById(String id) {
        Task task = getTaskById(id);
        task.setIsDeleted(true);
        taskRepository.save(task);
    }

    public Long getNextTaskNumberSequenceValue() {
        return (Long) entityManager.createNativeQuery("SELECT NEXTVAL('tasks_number_seq')")
                .getSingleResult();
    }

    private void removeTasksFromJobAndContact(Task taskFromDb) {
        Job job = taskFromDb.getJob();
        if (job != null) {
            job.getTasks().remove(taskFromDb);
            jobRepository.save(job);
        }
        Contact contact = taskFromDb.getContact();
        contact.getTasks().remove(taskFromDb);
        contactRepository.save(contact);
    }

    private void checkIfFollowsAlreadyPending(Task task, Task taskFromDb) {
        List<UUID> taskIds = taskRepository.findAllPendingIdsRecursive(task.getId());
        task.getFollows().forEach(parent -> {
            if (taskIds.contains(parent.getId())) throw new CircularLinkingException(
                    MessageFormat.format(
                            "Cannot make {0} pending {1}, " +
                            "because {1} follows {0}",
                            taskFromDb, parent)
            );
        });
    }

}
