package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.mapper.v1.TaskMapperV1;
import net.virtualboss.model.dto.TaskDto;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapperV1 taskMapper;

    public TaskDto[] findById(String id) {
        Task task = taskRepository.findById(UUID.fromString(id)).orElse(null);
        return new TaskDto[]{taskMapper.mapToDto(task)};
    }

    public List<TaskDto> findAll() {
        return taskMapper.map(taskRepository.findAll());
    }
}
