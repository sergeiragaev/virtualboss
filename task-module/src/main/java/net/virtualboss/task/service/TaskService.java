package net.virtualboss.task.service;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.CircularLinkingException;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.model.enums.*;
import net.virtualboss.common.repository.FieldRepository;
import net.virtualboss.common.service.GenericService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.util.*;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import net.virtualboss.task.mapper.v1.TaskMapperV1;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.mapper.v1.TaskResponseMapper;
import net.virtualboss.task.querydsl.TaskFilterCriteria;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class TaskService extends GenericService<Task, UUID, TaskResponse, QTask> {
    private final TaskRepository repository;
    private final JobRepository jobRepository;
    private final ContactRepository contactRepository;
    private final TaskMapperV1 mapper;
    private final TaskScheduleService taskScheduleService;
    private final WorkingDaysCalculator workingDaysCalculator;
    private final TaskResponseMapper taskResponseMapper;
    private final Map<String, String> customMappings = Map.of(
            "TaskCustom", "customFields.",
            "JobCustom", "job.customFields.",
            "ContactCustom", "contact.customFields."
    );
    private final Map<String, String> nestedMappings = Map.of(
            "Job", "job.",
            "Contact", "contact."
    );

    public TaskService(EntityManager entityManager,
                       MainService mainService,
                       TaskRepository taskRepository,
                       JobRepository jobRepository,
                       ContactRepository contactRepository,
                       TaskMapperV1 taskMapper,
                       TaskScheduleService taskScheduleService,
                       WorkingDaysCalculator workingDaysCalculator,
                       TaskResponseMapper taskResponseMapper,
                       FieldRepository fieldRepository) {
        super(entityManager, mainService, UUID::fromString, taskRepository, fieldRepository);
        this.repository = taskRepository;
        this.jobRepository = jobRepository;
        this.contactRepository = contactRepository;
        this.mapper = taskMapper;
        this.taskScheduleService = taskScheduleService;
        this.workingDaysCalculator = workingDaysCalculator;
        this.taskResponseMapper = taskResponseMapper;
    }


    @Override
    protected Map<String, String> getCustomMappings() {
        return customMappings;
    }

    @Override
    protected Map<String, String> getNestedMappings() {
        return nestedMappings;
    }

    @Override
    protected QTask getQEntity() {
        return QTask.task;
    }

    @Override
    protected Predicate buildFilterPredicate(CommonFilter filter) {
        TaskFilter taskFilter = (TaskFilter) filter;
        return TaskFilterCriteria.builder()
                .findString(StringUtils.defaultIfBlank(taskFilter.getFindString(), null))
                .status(determineTaskStatus(taskFilter))
                .marked(taskFilter.getIsMarked())
                .isDeleted(taskFilter.getIsDeleted())
                .targetStartFrom(getFilterDate(taskFilter, "targetStartFrom"))
                .targetStartTo(getFilterDate(taskFilter, "targetStartTo"))
                .targetFinishFrom(getFilterDate(taskFilter, "targetFinishFrom"))
                .targetFinishTo(getFilterDate(taskFilter, "targetFinishTo"))
                .actualFinishFrom(getFilterDate(taskFilter, "actualFinishFrom"))
                .actualFinishTo(getFilterDate(taskFilter, "actualFinishTo"))
                .anyDateFieldFrom(getFilterDate(taskFilter, "anyDateFieldFrom"))
                .anyDateFieldTo(getFilterDate(taskFilter, "anyDateFieldTo"))
                .jobList(getJobsByIds(taskFilter.getJobIds()))
                .contactList(getContactsByIds(taskFilter.getContactIds()))
                .taskList(getTaskIdsByNumbers(taskFilter.getTaskIds()))
                .excludeTaskIds(getExcludedTaskIds(taskFilter))
                .build()
                .toPredicate();
    }

    @Override
    protected String getCustomFieldPrefix() {
        return "TaskCustom";
    }

    @Override
    protected String getCustomFieldsAndListsPrefix() {
        return "TaskCustomFieldsAndLists";
    }

    @Override
    protected Set<String> parseFields(String fields) {
        return Arrays.stream(fields.split(","))
                .map(field -> {
                    if (field.contains(getCustomFieldPrefix())) return getCustomFieldsAndListsPrefix() + "." + field;
                    if (field.contains("JobCustom")) return "job.JobCustomFieldsAndLists." + field;
                    if (field.contains("ContactCustom")) return "contact.ContactCustomFieldsAndLists." + field;
                    if (field.startsWith("Job")) return "job." + field;
                    if (field.startsWith("Contact")) return "contact." + field;
                    return field;
                })
                .collect(Collectors.toSet());
    }

    @Override
    protected String getDefaultSort() {
        return "number";
    }

    @Override
    protected String getMustHaveFields() {
        return "TaskId,TaskDescription,JobId,ContactId";
    }

    @Override
    protected Class<Task> getEntityClass() {
        return Task.class;
    }

    @Override
    protected Class<TaskResponse> getResponseClass() {
        return TaskResponse.class;
    }

    @Override
    protected List<JoinExpression> getJoins() {
        QTask task = QTask.task;
        QJob job = QJob.job;
        QContact contact = QContact.contact;
        QFieldValue fieldValueTask = new QFieldValue("fieldValueTask");
        QFieldValue fieldValueJob = new QFieldValue("fieldValueJob");
        QFieldValue fieldValueContact = new QFieldValue("fieldValueContact");
        QField fieldTask = new QField("fieldTask");
        QField fieldJob = new QField("fieldJob");
        QField fieldContact = new QField("fieldContact");
        QTask taskFollows = new QTask("task_follows_0");
        return List.of(
                new CollectionJoin<>(task.customFieldsAndListsValues, fieldValueTask, JoinType.LEFTJOIN),
                new EntityJoin<>(fieldValueTask.field, fieldTask, JoinType.LEFTJOIN),
                new CollectionJoin<>(contact.customFieldsAndListsValues, fieldValueContact, JoinType.LEFTJOIN),
                new EntityJoin<>(fieldValueContact.field, fieldContact, JoinType.LEFTJOIN),
                new CollectionJoin<>(job.customFieldsAndListsValues, fieldValueJob, JoinType.LEFTJOIN),
                new EntityJoin<>(fieldValueJob.field, fieldJob, JoinType.LEFTJOIN),
                new CollectionJoin<>(QTask.task.follows, taskFollows, JoinType.LEFTJOIN)
        );
    }

    @Override
    protected List<GroupByExpression> getGroupBy() {
        QTask task = QTask.task;
        return List.of(
                new GroupByExpression(task.id),
                new GroupByExpression(task.job),
                new GroupByExpression(task.contact));
    }

    @Override
    public Page<Map<String, Object>> findAll(String fields, CommonFilter filter) {
        if (fields == null) fields = getMustHaveFields();
        TaskFilter taskFilter = (TaskFilter) filter;
        return super.findAll(fields, taskFilter);
    }

    @Cacheable(value = "task", key = "#id")
    public Map<String, Object> getById(String id) {
        Task task = findById(id);
        return taskResponseMapper.map(mapper.taskToResponse(task), null);
    }

    @Override
    protected void initializeFilterDefaults(CommonFilter filter) {
        if (filter.getLimit() == null) filter.setLimit(100);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort(getDefaultSort());
    }


    private TaskStatus determineTaskStatus(TaskFilter filter) {
        boolean isActive = Boolean.TRUE.equals(filter.getIsActive());
        boolean isDone = Boolean.TRUE.equals(filter.getIsDone());

        if (isDone && !isActive) return TaskStatus.DONE;
        if (isActive && !isDone) return TaskStatus.ACTIVE;
        return null;
    }

    private List<UUID> getExcludedTaskIds(TaskFilter filter) {
        if (filter.getLinkingTask() == null) return List.of();

        UUID linkingTaskId = UUID.fromString(filter.getLinkingTask());
        List<UUID> excludedIds = new ArrayList<>(repository.findAllPendingIdsRecursive(linkingTaskId));
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
        return repository.findAllByNumberIn(
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
        Task task = mapper.requestToTask(id, request, customFieldsAndLists, referenceRequest);
        Task taskFromDb = findById(id);
        checkIfFollowsAlreadyPending(task, taskFromDb);
        taskFromDb.getCustomFieldsAndListsValues().addAll(task.getCustomFieldsAndListsValues());
        removeTasksFromJobAndContact(taskFromDb);
        BeanUtils.copyNonNullProperties(task, taskFromDb);
        taskFromDb.setJob(task.getJob());
        taskFromDb.assignTasksToJobAndContact();
        taskScheduleService.recalculateTaskDates(taskFromDb);
        return taskResponseMapper.map(mapper.taskToResponse(taskFromDb), null);
    }

    @Transactional
    @CacheEvict(value = "task", allEntries = true)
    public Page<Map<String, Object>> updateTaskByStartAndFinish(
            String id,
            LocalDate targetStart,
            LocalDate targetFinish) {
        Task taskFromDb = findById(id);
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
        List<String> taskNumbers = new ArrayList<>(repository.findAllById(
                        repository.findAllPendingIdsRecursive(taskFromDb.getId()))
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
        Task task = mapper.requestToTask(request, customFieldsAndLists, referenceRequest);
        task.setNumber(getNextTaskNumberSequenceValue());
        task.setMarked(false);
        LocalDate validTargetStart =
                workingDaysCalculator.addWorkDays(
                        request.getTargetStart().minusDays(1), 1, "US");
        task.setTargetStart(validTargetStart);
        task.setTargetFinish(
                workingDaysCalculator.addWorkDays(validTargetStart, task.getDuration() - 1, "US"));
        entityManager.persist(task);
        return taskResponseMapper.map(
                mapper.taskToResponse(
                        repository.save(task)), null);
    }

    @Transactional
    @CacheEvict(value = "task", key = "#id")
    public void deleteTaskById(String id) {
        Task task = findById(id);
        task.setIsDeleted(true);
        repository.save(task);
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
        List<UUID> taskIds = repository.findAllPendingIdsRecursive(task.getId());
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
