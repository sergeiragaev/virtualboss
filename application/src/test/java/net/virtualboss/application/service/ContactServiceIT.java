package net.virtualboss.application.service;

import net.virtualboss.common.exception.AccessDeniedException;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.contact.service.ContactService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContactServiceIT extends TestDependenciesContainer {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ContactService contactService;
    @Autowired
    private MainService mainService;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearAllRepositories();
    }

    @Test
    @DisplayName("Get contact by ID returns valid contact")
    @Transactional
    void getContactById_ReturnsValidContact() {
        UpsertContactRequest request = generateTestContactRequest();
        CustomFieldsAndLists customFieldsAndLists = generateTestContactCustomFieldsRequest();
        Map<String, Object> savedContact = contactService.createContact(request, customFieldsAndLists);
        Contact result = mainService.getContactById(savedContact.get("ContactId").toString());
        assertEquals(savedContact.get("ContactId"), result.getId());
        assertEquals(request.getCompany(), result.getCompany());
        assertEquals(customFieldsAndLists.getCustomField4(),
                result.getCustomValueByName("ContactCustomField4"));
    }

    @Test
    @DisplayName("Update contact correctly")
    @Transactional
    void updateContact_CorrectUpdate() {
        CustomFieldsAndLists customFieldsAndLists = generateTestContactCustomFieldsRequest();
        Map<String, Object> savedContact = contactService.createContact(
                generateTestContactRequest(), customFieldsAndLists);
        String id = savedContact.get("ContactId").toString();
        UpsertContactRequest updatedRequest = UpsertContactRequest.builder()
                .id(UUID.fromString(id))
                .company("Updated contact company")
                .build();
        customFieldsAndLists.setCustomList4("Updated Contact custom list4");
        contactService.saveContact(id, updatedRequest, customFieldsAndLists);
        Contact updatedContact = contactRepository.findById(UUID.fromString(id)).orElseThrow();
        assertEquals(updatedRequest.getCompany(), updatedContact.getCompany());
        assertEquals(customFieldsAndLists.getCustomList4(), updatedContact.getCustomValueByName("ContactCustomList4"));
        assertEquals("contact custom field 3", updatedContact.getCustomValueByName("ContactCustomField3"));
    }

    @Test
    @DisplayName("Update Contact failed while trying set some properties to Unassigned contact")
    @Transactional
    void updateUnassignedContact() {
        UpsertContactRequest request = generateTestContactRequest();
        CustomFieldsAndLists customFL = generateTestContactCustomFieldsRequest();
        Contact unassignedContact = mainService.getContactById(null);
        assertEquals(1, contactRepository.count());
        String contactId = unassignedContact.getId().toString();
        assertThrows(AccessDeniedException.class,
                () -> contactService.saveContact(contactId, request, customFL));
    }

    @Test
    @DisplayName("Delete contact correctly")
    @Transactional
    void deleteContact_CorrectDelete() {
        Map<String, Object> savedTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        String taskId = savedTask.get("TaskId").toString();
        String contactId = savedTask.get("ContactId").toString();
        Contact taskContact = contactRepository.findById(UUID.fromString(contactId)).orElseThrow();
        assertEquals(taskRepository.findById(UUID.fromString(taskId)).orElseThrow().getContact(), taskContact);
        contactService.deleteContact(contactId);
        assertTrue(contactRepository.findById(UUID.fromString(contactId)).orElseThrow().getIsDeleted());
        assertEquals(taskRepository.findById(UUID.fromString(taskId)).orElseThrow().getContact(),
                contactRepository.getUnassigned().orElseThrow());
    }

    @Test
    @DisplayName("Search contacts with specific word in custom fields")
    @Transactional
    void searchContacts() {
        contactService.createContact(generateTestContactRequest(), generateTestContactCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        filter.setFindString("custom");
        Page<Map<String, Object>> result = contactService.findAll(null, filter);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search contacts with not filter set")
    @Transactional
    void searchAllContacts() {
        contactService.createContact(generateTestContactRequest(), generateTestContactCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        Page<Map<String, Object>> result = contactService.findAll(null, filter);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search contacts with blank filter find string")
    @Transactional
    void searchAllWithBlankContacts() {
        contactService.createContact(generateTestContactRequest(), generateTestContactCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        filter.setFindString(" ");
        Page<Map<String, Object>> result = contactService.findAll(null, filter);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Search specific contact by filters")
    @Transactional
    void searchSpecificContactByFilters() {
        Map<String, Object> savedContactMap = contactService.createContact(generateTestContactRequest(), generateTestContactCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        String savedContactCompany = savedContactMap.get("ContactCompany").toString();
        String savedContactId = savedContactMap.get("ContactId").toString();
        filter.setFindString(savedContactCompany);
        filter.setIsDeleted(false);
        Page<Map<String, Object>> result = contactService.findAll("ContactId", filter);
        assertNotNull(result);
        assertFalse(result.getContent().get(0).isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(savedContactId, result.getContent().get(0).get("ContactId").toString());
    }

    @Test
    @DisplayName("Search Contact with non-matching filters")
    @Transactional
    void searchContactWithNonMatchingFilters() {
        Map<String, Object> savedContactMap = contactService.createContact(generateTestContactRequest(), generateTestContactCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        String savedContactId = savedContactMap.get("ContactId").toString();
        filter.setIsDeleted(false);
        filter.setFindString(savedContactId);
        Page<Map<String, Object>> result = contactService.findAll("ContactId", filter);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}