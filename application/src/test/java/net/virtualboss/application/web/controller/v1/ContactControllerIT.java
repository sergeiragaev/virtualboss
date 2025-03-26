package net.virtualboss.application.web.controller.v1;

import net.virtualboss.application.service.TestDependenciesContainer;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.contact.service.ContactService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ContactControllerIT extends TestDependenciesContainer {
    @Autowired
    private ContactService contactService;

    @BeforeEach
    void initBeforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearAllRepositories();
    }

    @Test
    @DisplayName("test get contact by id")
    void getContactById_ReturnsValidContact() throws Exception {
        Contact contact = saveContactInDbAndGet(generateTestContactRequest(), generateTestContactCustomFieldsRequest());
        String customValue = contact.getCustomValueByName("ContactCustomField3");
        mockMvc.perform(get("/contact/" + contactRepository.findAll().get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ContactCompany").value(contact.getCompany()))
                .andExpect(jsonPath("$.ContactCustomField3")
                        .value(customValue))
                .andReturn();
    }

    @Test
    @DisplayName("contact successfully deleted test")
    void deleteContactById_CorrectDelete() throws Exception {
        Contact contact = saveAndGetTestContactToDelete();
        mockMvc.perform(delete("/contact/" + contact.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("contact deletion failed of fake id test")
    void deleteContactById_NotFound() throws Exception {
        Contact contact = saveAndGetTestContactToDelete();
        contact.setId(UUID.randomUUID());
        mockMvc.perform(delete("/contact/" + contact.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("creating contact test")
    void createContact() throws Exception {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder()
                .customField1("test custom field1")
                .customField2("test custom field2")
                .build();
        String jsonString = objectMapper.writeValueAsString(generateTestContactRequest());
        String queryString = getQueryString(jsonString, false);
        String customQueryString = getQueryString(
                objectMapper.writeValueAsString(customFieldsAndLists), true);

        mockMvc.perform(post("/contact" + queryString + customQueryString)
//                        .header("id", 1L)
                )
                .andExpect(jsonPath("$.ContactGroups").value("Test contact group"))
                .andExpect(jsonPath("$.ContactCustomField1").value(customFieldsAndLists.getCustomField1()))
                .andExpect(jsonPath("$.ContactCustomField2").value(customFieldsAndLists.getCustomField2()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("update contact company and custom field1 value is correct test")
    void updateContactCompanyById_CorrectUpdate() throws Exception {
        Contact newContact = saveAndGetTestContactToUpdate();
        UpsertContactRequest updatedRequest = getUpdatedContactRequestByContact(newContact);
        String updatedJson = objectMapper.writeValueAsString(updatedRequest);
        String updatedQueryString = getQueryString(updatedJson, false);
        CustomFieldsAndLists customFL = generateTestContactCustomFieldsRequest();
        customFL.setCustomField1("new contact custom field 1 value");
        String updatedCustomFL = getQueryString(objectMapper.writeValueAsString(customFL), true);
        mockMvc.perform(put("/contact/" + contactRepository.findAll().get(0).getId() +
                                updatedQueryString +
                                updatedCustomFL)
//                        .header("id", 1L)
                )
                .andExpect(jsonPath("$.ContactCustomField1").value(
                        customFL.getCustomField1()))
                .andExpect(jsonPath("$.ContactCompany").value(
                        updatedRequest.getCompany()))
                .andExpect(status().isOk()
                );
    }

    @Test
    @DisplayName("search contacts with specific criteria api test")
    void searchContacts() throws Exception {
        UpsertContactRequest testRequest = generateTestContactRequest();
        saveContactInDbAndGet(testRequest, generateTestContactCustomFieldsRequest());
        mockMvc.perform(get("/contact")
                        .param("fields", "ContactId,ContactCompany,ContactCustomList5,ContactPerson")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(10))
                        .param("sort", "firstName asc,lastName asc,company asc")
                        .param("findString", "custom field")
                )
                .andExpect(jsonPath("[0].ContactCompany").value(testRequest.getCompany()))
                .andExpect(jsonPath("[0].ContactCustomList5").value("contact custom list 5"))
                .andExpect(jsonPath("[0].ContactPerson").value("First name Last name"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("search deleted contact test")
    void searchDeletedContact() throws Exception {
        UpsertContactRequest testRequest = generateTestContactRequest();
        Contact contact = saveContactInDbAndGet(testRequest, generateTestContactCustomFieldsRequest());
        contactService.deleteContact(contact.getId().toString());
        mockMvc.perform(get("/contact")
                        .param("fields", "ContactId,ContactCompany")
                        .param("isDeleted", String.valueOf(true))
                        .param("findString", "custom list")
                )
                .andExpect(jsonPath("[0].ContactCompany").value(testRequest.getCompany()))
                .andExpect(status().isOk());
    }

    //-------------------------UTIL-METHODS------------------------------

    private Contact saveAndGetTestContactToUpdate() {
        return saveContactInDbAndGet(UpsertContactRequest.builder()
                        .company("new contact company")
                        .firstName("new first name")
                        .build(),
                generateTestContactCustomFieldsRequest());
    }

    private UpsertContactRequest getUpdatedContactRequestByContact(Contact newContact) {
        String updatedContactCompany = "updated contact company";
        return UpsertContactRequest.builder()
                .company(updatedContactCompany)
                .id(newContact.getId())
                .build();
    }

    private Contact saveAndGetTestContactToDelete() {
        return saveTaskInDbAndGet(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest()).getContact();
    }
}