package net.virtualboss.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.CircularLinkingException;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.mapper.v1.task.TaskMapperV1;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.enums.DateCriteria;
import net.virtualboss.model.enums.DateRange;
import net.virtualboss.model.enums.DateType;
import net.virtualboss.model.enums.TaskStatus;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.TaskReferencesRequest;
import net.virtualboss.web.dto.task.TaskResponse;
import net.virtualboss.web.dto.task.TaskFilter;
import net.virtualboss.repository.criteria.TaskFilterCriteria;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.repository.TaskRepository;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
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
        Set<String> fieldSet = Arrays.stream(fields.split(",")).collect(Collectors.toSet());

        if (filter.getSize() == null) filter.setSize(100);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("description asc");

        String[] sorts = filter.getSort().split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] order = sort.split(" ");
            orders.add(new Sort.Order(Sort.Direction.valueOf(order[1].toUpperCase()), order[0]));
        }

        TaskStatus status = null;
        boolean isActive = filter.getIsActive() != null && filter.getIsActive();
        boolean isDone = filter.getIsDone() != null && filter.getIsDone();
        if (!isActive && isDone) {
            status = TaskStatus.DONE;
        } else if (isActive && !isDone) {
            status = TaskStatus.ACTIVE;
        }

        if (filter.getFindString() == null) filter.setFindString("");

        Map<String, LocalDate> filterDates = setFilterDates(filter);

        List<UUID> excludeTaskIds = new ArrayList<>();
        if (filter.getLinkingTask() != null) {
            excludeTaskIds =
                    taskRepository.findAllPendingIdsRecursive(UUID.fromString(filter.getLinkingTask()));
            excludeTaskIds.add(UUID.fromString(filter.getLinkingTask()));
        }

        return taskRepository.findAll(
                        TaskFilterCriteria.builder()
                                .findString(filter.getFindString().isBlank() ? null : filter.getFindString())
                                .status(status)
                                .marked(filter.getIsMarked())
                                .isDeleted(filter.getIsDeleted())
                                .targetStartFrom(filterDates.get("targetStartFrom"))
                                .targetStartTo(filterDates.get("targetStartTo"))
                                .targetFinishFrom(filterDates.get("targetFinishFrom"))
                                .targetFinishTo(filterDates.get("targetFinishTo"))
                                .actualFinishFrom(filterDates.get("actualFinishFrom"))
                                .actualFinishTo(filterDates.get("actualFinishTo"))
                                .anyDateFieldFrom(filterDates.get("anyDateFieldFrom"))
                                .anyDateFieldTo(filterDates.get("anyDateFieldTo"))
                                .jobList(filter.getJobIds() == null ? null :
                                        jobRepository.findAllById(
                                                filter.getJobIds().stream()
                                                        .map(UUID::fromString).toList()))
                                .contactList(filter.getContactIds() == null ? null :
                                        contactRepository.findAllById(
                                                filter.getContactIds().stream()
                                                        .map(UUID::fromString).toList()))
                                .taskList(filter.getTaskIds() == null ? null :
                                        taskRepository.findAllByNumberIn(
                                                        filter.getTaskIds().stream()
                                                                .map(Long::valueOf)
                                                                .toList())
                                                .stream().map(Task::getId).toList())
                                .excludeTaskIds(excludeTaskIds)
                                .build().getSpecification(),
                        PageRequest.of(filter.getPage() - 1, filter.getSize(),
                                Sort.by(orders)
                        ))
                .getContent().stream()
                .map(taskMapper::taskToResponse)
                .map(taskResponse -> TaskResponse.getFieldsMap(taskResponse, fieldSet))
                .toList();
    }

    private Map<String, LocalDate> setFilterDates(TaskFilter filter) {
        Map<String, LocalDate> filterDates = new HashMap<>();

        if (filter.getIsDateRange() != null) {
            Map<Integer, String> dateFields = new HashMap<>();
            dateFields.put(DateType.TARGET_START.getValue(), "targetStart");
            dateFields.put(DateType.TARGET_FINISH.getValue(), "targetFinish");
            dateFields.put(DateType.ACTUAL_FINISH.getValue(), "actualFinish");
            dateFields.put(DateType.ANY_DATE_FIELD.getValue(), "anyDateField");

            if (filter.getDateRange() == DateRange.TODAY.getValue()
                    || filter.getDateRange() == DateRange.EXACT_DATE.getValue()) {
                if (filter.getDateCriteria() == DateCriteria.ON_OR_BEFORE.getValue()) {
                    filter.setDateTo(filter.getThisDate());
                } else if (filter.getDateCriteria() == DateCriteria.ON_OR_AFTER.getValue()) {
                    filter.setDateFrom(filter.getThisDate());
                } else {
                    filter.setDateTo(filter.getThisDate());
                    filter.setDateFrom(filter.getThisDate());
                }
            }

            if (filter.getDateCriteria() == DateCriteria.ON_OR_BEFORE.getValue()) { //on or before
                filterDates.put(dateFields.get(filter.getDateType()) + "To", LocalDate.from(filter.getDateTo()));
            } else if (filter.getDateCriteria() == DateCriteria.ON_OR_AFTER.getValue()) { //on or after
                filterDates.put(dateFields.get(filter.getDateType()) + "From", LocalDate.from(filter.getDateFrom()));
            } else if (filter.getDateCriteria() == DateCriteria.EXACT.getValue()) {    //Exact
                filterDates.put(dateFields.get(filter.getDateType()) + "From", LocalDate.from(filter.getDateFrom()));
                filterDates.put(dateFields.get(filter.getDateType()) + "To", LocalDate.from(filter.getDateTo()));
            }
        }
        return filterDates;
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
