package net.virtualboss.mapper.v1;

import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.repository.EmployeeRepository;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.web.dto.TaskDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public abstract class TaskMapperDelegate implements TaskMapperV1 {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Override
    public Task mapToEntity(TaskDto taskDto) {
        return Task.builder()
                .id(taskDto.getId())
                .status(taskDto.getStatus())
                .contact(contactRepository.findById(UUID.fromString(taskDto.getContactId())).orElseThrow(
                        () -> new EntityNotFoundException("Contact with such id not found: " + taskDto.getContactId())
                ))
                .job(jobRepository.findByNumber(taskDto.getJobNumber()).orElseThrow(
                        () -> new EntityNotFoundException("Job with such number not found: " + taskDto.getJobNumber())
                ))
                .requested(employeeRepository.findByName(taskDto.getRequested()).orElseThrow(
                        () -> new EntityNotFoundException("Employee with such name not found: " + taskDto.getRequested())
                ))
                .description(taskDto.getDescription())
                .notes(taskDto.getNotes())
                .order(taskDto.getOrder())
                .duration(taskDto.getDuration())
                .targetStart(taskDto.getTargetStart())
                .targetFinish(taskDto.getTargetFinish())
                .actualFinish(taskDto.getActualFinish())
                .marked(taskDto.getMarked())
                .build();
    }
}
