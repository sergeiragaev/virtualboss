package net.virtualboss.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.common.repository.ContactRepository;
import net.virtualboss.common.repository.JobRepository;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class MainService {
    private final TaskRepository taskRepository;
    private final JobRepository jobRepository;
    private final ContactRepository contactRepository;

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
        contact.setCompany("Unassigned");
        return contactRepository.save(contact);
    }

    public Job getJobByNumber(String jobNumber) {
        if (jobNumber == null || jobNumber.isBlank()) return null;
        return jobRepository.findByNumberIgnoreCaseAndIsDeleted(jobNumber, false).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormat.format("Job with number: {0} not found!", jobNumber)));
    }

    public Set<Task> createFollows(String follows) {
        if (follows == null || follows.isBlank()) return new HashSet<>();
        Set<Task> set = new HashSet<>();
        for (String id : follows.split(",")) {
            Task task = taskRepository.findByNumber(Long.parseLong(id)).orElseThrow(
                    () -> new EntityNotFoundException(
                            MessageFormat.format("Task with number {0} not found!", id)
                    )
            );
            set.add(task);
        }
        return set;
    }
}
