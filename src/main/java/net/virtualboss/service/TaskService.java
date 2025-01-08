package net.virtualboss.service;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.mapper.v1.task.TaskMapperV1;
import net.virtualboss.model.entity.Group;
import net.virtualboss.model.enums.DateCriteria;
import net.virtualboss.model.enums.DateRange;
import net.virtualboss.model.enums.DateType;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.web.dto.CustomFieldsAndLists;
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
import org.springframework.http.converter.json.MappingJacksonValue;
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

        if (filter.getSize() == null) filter.setSize(Integer.MAX_VALUE);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("description asc");

        String[] sorts = filter.getSort().split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] order = sort.split(" ");
            orders.add(new Sort.Order(Sort.Direction.valueOf(order[1].toUpperCase()), order[0]));
        }

        String status = null;
        boolean isActive = filter.getIsActive() != null && filter.getIsActive();
        boolean isDone = filter.getIsDone() != null && filter.getIsDone();
        if (!isActive && isDone) {
            status = "Done";
        } else if (isActive && !isDone) {
            status = "Active";
        }

        if (filter.getFindString() == null) filter.setFindString("");

        Map<String, LocalDate> filterDates = setFilterDates(filter);

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
                                        filter.getTaskIds().stream()
                                                .map(UUID::fromString).toList())
                                .build().getSpecification(),
                        PageRequest.of(filter.getPage() - 1, filter.getSize(),
                                Sort.by(orders)
                        ))
                .map(taskMapper::taskToResponse).getContent().stream()
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
    public Map<String, Object> saveTask(String id, UpsertTaskRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Task task = taskMapper.requestToTask(id, request, customFieldsAndLists);
        Task taskFromDb = getTaskById(id);
        task.getCustomFieldsAndListsValues().addAll(taskFromDb.getCustomFieldsAndListsValues());
        BeanUtils.copyNonNullProperties(task, taskFromDb);
        return TaskResponse.getFieldsMap(taskMapper.taskToResponse(taskRepository.save(taskFromDb)), null);
    }

    @Transactional
    @CacheEvict(value = "task", allEntries = true)
    public Map<String, Object> createNewTask(
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        Task task = taskMapper.requestToTask(request, customFieldsAndLists);
        task.setNumber(getNextNumberSequenceValue());
        return TaskResponse.getFieldsMap(taskMapper.taskToResponse(taskRepository.save(task)), null);
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

    public static String mapGroupsToResponse(Task task) {
        return task.getGroups() == null ? null :
                task.getGroups().stream().map(Group::getName)
                        .collect(Collectors.joining(","));
    }

    public MappingJacksonValue retrieveTaskValues(TaskResponse taskResponse, Set<String> fields) {
        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept(fields);
        FilterProvider filters = new SimpleFilterProvider().addFilter("TaskResponseFilter", filter);
        MappingJacksonValue mapping = new MappingJacksonValue(taskResponse);
        mapping.setFilters(filters);
        return mapping;
    }
}
