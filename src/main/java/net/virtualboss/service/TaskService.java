package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.mapper.v1.TaskMapperV1;
import net.virtualboss.web.dto.TaskDto;
import net.virtualboss.web.dto.TaskFilterDto;
import net.virtualboss.web.criteria.TaskFilterCriteria;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
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



    @Cacheable(value = "task", key = "#id")
    public TaskDto[] findById(String id) {
        Task task = taskRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Task with id: {0} not found", id)));
        return new TaskDto[]{taskMapper.mapToDto(task)};
    }

    public List<Map<String, Object>> findAll(String fields, TaskFilterDto filter, Integer size, Integer page) {

        List<String> fieldList = Arrays.stream(fields.split(",")).toList();

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
                                .findString(filter.getFindString())
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
                        PageRequest.of(page - 1, size))
                .map(taskMapper::mapToDto).getContent().stream()
                .map(taskDto -> TaskDto.getFieldsMap(taskDto, true, fieldList))
                .toList();
    }

    private Map<String, LocalDate> setFilterDates(TaskFilterDto filter) {
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
    @CacheEvict(value = "task", key = "#taskDto.id")
    public TaskDto[] saveTask(TaskDto taskDto) {
        Task task = taskMapper.mapToEntity(taskDto);
        return new TaskDto[]{taskMapper.mapToDto(taskRepository.save(task))};
    }
}
