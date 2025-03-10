package net.virtualboss.task.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.CircularLinkingException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.task.mapper.v1.TaskMapperV1;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.common.model.enums.DateCriteria;
import net.virtualboss.common.model.enums.DateRange;
import net.virtualboss.common.model.enums.DateType;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.TaskResponse;
import net.virtualboss.task.web.dto.TaskFilter;
import net.virtualboss.task.repository.criteria.TaskFilterCriteria;
import net.virtualboss.common.model.entity.Task;
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
import org.springframework.data.jpa.domain.Specification;
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
        Set<String> fieldSet = parseFields(fields);
        initializeFilterDefaults(filter);
        PageRequest pageRequest = createPageRequest(filter);

        return taskRepository.findAll(
                        buildTaskFilterCriteria(filter),
                        pageRequest
                ).getContent()
                .stream()
                .map(taskMapper::taskToResponse)
                .map(response -> TaskResponse.getFieldsMap(response, fieldSet))
                .toList();
    }

    private Set<String> parseFields(String fields) {
        String defaultFields = "TaskId,TaskDescription";
        return Arrays.stream((fields != null ? fields : defaultFields).split(","))
                .collect(Collectors.toSet());
    }

    private void initializeFilterDefaults(TaskFilter filter) {
        if (filter.getSize() == null) filter.setSize(100);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("description asc");
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

    private Specification<Task> buildTaskFilterCriteria(TaskFilter filter) {
        return TaskFilterCriteria.builder()
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
                .build()
                .getSpecification();
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
        Task.recalculate(taskFromDb);
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
            int workingDays = getWorkingDays(taskFromDb.getTargetStart(), targetStart);
            if (!taskFromDb.getFollows().isEmpty()) {
                taskFromDb.setFinishPlus(taskFromDb.getFinishPlus() + workingDays);
            }
            if (targetFinish == null) {
                taskFromDb.setDuration(taskFromDb.getDuration() - workingDays);
            }
            taskFromDb.setTargetStart(targetStart);
        } else {
            int workingDays = getWorkingDays(taskFromDb.getTargetFinish(), targetFinish);
            taskFromDb.setDuration(taskFromDb.getDuration() + workingDays);
        }
        Task.recalculate(taskFromDb);
        TaskFilter filter = new TaskFilter();
        List<String> taskNumbers = new ArrayList<>(taskRepository.findAllById(
                        taskRepository.findAllPendingIdsRecursive(taskFromDb.getId()))
                .stream().map(Task::getNumber).map(String::valueOf).toList());
        taskNumbers.add(taskFromDb.getNumber().toString());
        filter.setTaskIds(taskNumbers);
        return this.findAll("TaskId,TaskTargetStart,TaskTargetFinish,TaskDuration", filter);
    }

    private int getWorkingDays(LocalDate oldDate, LocalDate newDate) {
        if (oldDate.isAfter(newDate)) {
            return -(int) newDate.datesUntil(oldDate).filter(date -> {
                int dow = date.getDayOfWeek().getValue();
                return !(dow == 6 || dow == 7);
            }).count();
        } else {
            return (int) oldDate.datesUntil(newDate).filter(date -> {
                int dow = date.getDayOfWeek().getValue();
                return !(dow == 6 || dow == 7);
            }).count();
        }
    }

    @Transactional
    @CacheEvict(value = "task", allEntries = true)
    public Map<String, Object> createNewTask(
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists,
            TaskReferencesRequest referenceRequest) {
        Task task = taskMapper.requestToTask(request, customFieldsAndLists, referenceRequest);
        task.setNumber(getNextNumberSequenceValue());
        task.setTargetStart(request.getTargetStart());
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

    public Long getNextNumberSequenceValue() {
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
