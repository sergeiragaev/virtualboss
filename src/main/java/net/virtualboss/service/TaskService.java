package net.virtualboss.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.mapper.v1.TaskMapperV1;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Job;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.web.dto.task.TaskResponse;
import net.virtualboss.web.dto.task.TaskFilter;
import net.virtualboss.web.criteria.TaskFilterCriteria;
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
    public TaskResponse findById(String id) {
        Task task = getTaskById(id);
        return taskMapper.taskToResponse(task);
    }

    private Task getTaskById(String id) {
        return taskRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Task with id: {0} not found!", id)));
    }

    public List<Map<String, Object>> findAll(String fields, TaskFilter filter) {

        List<String> fieldList = Arrays.stream(fields.split(",")).toList();

        if (filter.getSize() == null) filter.setSize(Integer.MAX_VALUE);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("description,asc");
        String[] sorts = filter.getSort().split(",");

        String status = null;
        boolean isActive = filter.getIsActive() != null && filter.getIsActive();
        boolean isDone = filter.getIsDone() != null && filter.getIsDone();
        if (!isActive && isDone) {
            status = "Done";
        } else if (isActive && !isDone) {
            status = "Active";
        }

        Map<String, LocalDate> filterDates = setFilterDates(filter);

        return taskRepository.findAll(
                        TaskFilterCriteria.builder()
                                .findString(filter.getFindString().isBlank() ? null : filter.getFindString())
                                .status(status)
                                .marked(filter.getIsMarked())
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
                                .contactList(filter.getCustIds() == null ? null :
                                        contactRepository.findAllById(
                                                filter.getCustIds().stream()
                                                        .map(UUID::fromString).toList()))
                                .build().getSpecification(),
                        PageRequest.of(filter.getPage() - 1, filter.getSize(),
                                Sort.by(
                                        Sort.Direction.valueOf(sorts[1].toUpperCase()), sorts[0]
                                )
                        ))
                .map(taskMapper::taskToResponse).getContent().stream()
                .map(taskResponse -> TaskResponse.getFieldsMap(taskResponse, fieldList))
                .toList();
    }

    private Map<String, LocalDate> setFilterDates(TaskFilter filter) {
        Map<String, LocalDate> filterDates = new HashMap<>();

        if (filter.getIsDateRange() != null) {
            Map<Integer, String> dateFields = new HashMap<>();
            dateFields.put(1, "targetStart");
            dateFields.put(2, "targetFinish");
            dateFields.put(3, "actualFinish");
            dateFields.put(4, "anyDateField");

            if (filter.getDateRange() == 5 || filter.getDateRange() == 1) {
                if (filter.getDateCriteria() == 1) {
                    filter.setDateTo(filter.getThisDate());
                } else if (filter.getDateCriteria() == 2) {
                    filter.setDateFrom(filter.getThisDate());
                } else {
                    filter.setDateTo(filter.getThisDate());
                    filter.setDateFrom(filter.getThisDate());
                }
            }

            if (filter.getDateCriteria() == 1) { //on or before
                filterDates.put(dateFields.get(filter.getDateType()) + "To", LocalDate.from(filter.getDateTo()));
            } else if (filter.getDateCriteria() == 2) { //on or after
                filterDates.put(dateFields.get(filter.getDateType()) + "From", LocalDate.from(filter.getDateFrom()));
            } else if (filter.getDateCriteria() == 3) {    //Exact
                filterDates.put(dateFields.get(filter.getDateType()) + "From", LocalDate.from(filter.getDateFrom()));
                filterDates.put(dateFields.get(filter.getDateType()) + "To", LocalDate.from(filter.getDateTo()));
            }
        }
        return filterDates;
    }

    @Transactional
    @CachePut(value = "task", key = "#id")
    public TaskResponse saveTask(String id, UpsertTaskRequest request) {
        Task task = taskMapper.requestToTask(id, request);
        Task taskFromDb = getTaskById(id);
        BeanUtils.copyNonNullProperties(task, taskFromDb);
        return taskMapper.taskToResponse(taskRepository.save(taskFromDb));
    }

    @Transactional
    public TaskResponse createTask(UpsertTaskRequest request) {
        Task task = taskMapper.requestToTask(request);
        task.setNumber(getNextSequenceValue("tasks_number_seq"));
        return taskMapper.taskToResponse(taskRepository.save(task));
    }

    @Transactional
    @CacheEvict(value = "task", key = "#id")
    public void deleteTask(String id) {
        Task task = taskRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormat.format("Task with Id: {0} not found!", id)
                )
        );
        taskRepository.delete(task);
    }

    public void eraseJobFromTasks(Job job) {
        taskRepository.findAllByJob(job)
                .forEach(this::eraseJobFromTask);
    }

    @CacheEvict(value = "task", key = "#task.id")
    public void eraseJobFromTask(Task task) {
        task.setJob(null);
        taskRepository.save(task);
    }

    public void reassignTasksContact(Contact contact) {
        taskRepository.findAllByContact(contact)
                .forEach(this::updateTasksContactToUnassigned);
    }

    @CacheEvict(value = "task", key = "#task.id")
    public void updateTasksContactToUnassigned(Task task) {
        task.setContact(getContactById(null));
        taskRepository.save(task);
    }

    public Contact getContactById(String contactId) {
        if (contactId == null || contactId.isBlank() || contactId.equals("UNASSIGNED"))
            return contactRepository.getUnassigned().orElseGet(this::createUnassigned);
        return contactRepository.findById(UUID.fromString(contactId)).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormat.format("Contact with id: {0} not found!", contactId)));

    }

    private Contact createUnassigned() {
        Contact contact = new Contact();
        contact.setCompany("UNASSIGNED");
        return contactRepository.save(contact);
    }

    public Job getJobByNumber(String jobNumber) {
        if (jobNumber == null || jobNumber.isBlank()) return null;
        return jobRepository.findByNumberIgnoreCase(jobNumber).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormat.format("Job with number: {0} not found!", jobNumber)));
    }

    public Long getNextSequenceValue(String sequenceName) {
        return (Long) entityManager.createNativeQuery("SELECT NEXTVAL(:sequenceName)")
                .setParameter("sequenceName", sequenceName)
                .getSingleResult();
    }
}
