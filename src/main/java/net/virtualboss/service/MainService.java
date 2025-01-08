package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.model.entity.*;
import net.virtualboss.repository.*;
import net.virtualboss.web.dto.CustomFieldsAndLists;
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
    private final FieldRepository fieldRepository;
    private final FieldValueRepository fieldValueRepository;

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
        return jobRepository.findByNumberIgnoreCaseAndIsDeleted(jobNumber, false).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormat.format("Job with number: {0} not found!", jobNumber)));
    }

    public Set<FieldValue> createCustomList(CustomFieldsAndLists customFieldsAndLists, String prefix) {
        Set<FieldValue> values = new HashSet<>();
        Map<String, String> customFieldsMap =
                CustomFieldsAndLists.getFieldsMap(customFieldsAndLists, prefix, null);
        for (Map.Entry<String, String> entry : customFieldsMap.entrySet()) {
            String fieldCaption = entry.getKey();
            if (entry.getValue() == null) continue;
            String fieldValue = entry.getValue();
            if (!fieldValue.isBlank()) {
                Field field = fieldRepository
                        .findByName(fieldCaption)
                        .orElseThrow(() -> new EntityNotFoundException(
                                MessageFormat.format("Field with name {0} not found!", fieldCaption)) );
                FieldValue value = fieldValueRepository
                        .findByFieldAndValue(field, fieldValue).orElse(
                                FieldValue.builder()
                                        .field(field)
                                        .value(fieldValue)
                                        .build()
                        );

                values.add(value);
            }
        }
        return values;
    }

    public CustomFieldsAndLists setCustomFieldsAndLists(Set<FieldValue> values, String prefix) {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();
        CustomFieldsAndLists.setCustomFieldsAndListsValues(customFieldsAndLists, values, prefix);
        return customFieldsAndLists;
    }
}
