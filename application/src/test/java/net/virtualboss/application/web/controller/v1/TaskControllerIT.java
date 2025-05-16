package net.virtualboss.application.web.controller.v1;

import net.virtualboss.application.service.TestDependenciesContainer;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.model.enums.DateCriteria;
import net.virtualboss.common.model.enums.DateRange;
import net.virtualboss.common.model.enums.DateType;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TaskControllerIT extends TestDependenciesContainer {
    @BeforeEach
    void initBeforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearAllRepositories();
    }

    @Test
    @DisplayName("test get task by id test")
    @Transactional
    void getTaskById_ReturnsValidTask() throws Exception {
        Task task = saveTaskInDbAndGet(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        String customValue = task.getCustomValueByName("TaskCustomField1");
        mockMvc.perform(get("/task/" + taskRepository.findAll().get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TaskDescription").value(task.getDescription()))
                .andExpect(jsonPath("$.TaskCustomField1")
                        .value(customValue))
                .andReturn();
    }

    @Test
    @DisplayName("task successfully deleted test")
    @Transactional
    void deleteTaskById_CorrectDelete() throws Exception {
        Task task = saveAndGetTestTaskToDelete();
        mockMvc.perform(delete("/task/" + task.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("task deletion failed of fake id test")
    @Transactional
    void deleteTaskById_NotFound() throws Exception {
        Task task = saveAndGetTestTaskToDelete();
        task.setId(UUID.randomUUID());
        mockMvc.perform(delete("/task/" + task.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("search tasks with specific criteria api test")
    @Transactional
    void searchTasks() throws Exception {
        UpsertTaskRequest testTaskRequest = generateTestTaskRequest();
        TaskReferencesRequest testTaskReference = generateTestTaskReferenceRequest();
        saveTaskInDbAndGet(testTaskRequest,
                generateTestTaskCustomFieldsRequest(),
                testTaskReference);
        mockMvc.perform(get("/task")
                        .param("fields", "TaskId,TaskDescription,TaskCustomField1,TaskStatus," +
                                         "JobCustomList1,ContactCustomField4,JobNumber,ContactCompany,Color")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(10))
                        .param("sort", "TaskNumber:asc,ContactCustomField4:desc,TaskCustomField1:asc")
                        .param("isActive", String.valueOf(true))
                        .param("isDone", String.valueOf(true))
                        .param("dateType", String.valueOf(DateType.TARGET_START.getValue()))
                        .param("isDateRange", String.valueOf(true))
                        .param("dateRange", String.valueOf(DateRange.DATE_PERIOD.getValue()))
                        .param("dateFrom", LocalDate.now().minusDays(10).toString())
                        .param("dateTo", LocalDate.now().plusDays(10).toString())
                        .param("dateCriteria", String.valueOf(DateCriteria.EXACT.getValue()))
                        .param("jobIds", String.valueOf(
                                jobRepository.findByNumberIgnoreCaseAndIsDeleted(testTaskReference.getJobNumber(), false).orElseThrow().getId()))
                        .param("contactIds", String.valueOf(testTaskReference.getContactId()))
                        .param("findString", "Subdivision")
                )
                .andExpect(jsonPath("content.[0].TaskDescription").value(testTaskRequest.getDescription()))
                .andExpect(jsonPath("content.[0].TaskStatus").value(testTaskRequest.getStatus().toCamelCase()))
                .andExpect(jsonPath("content.[0].JobNumber").value(testTaskReference.getJobNumber()))
                .andExpect(jsonPath("content.[0].ContactCompany").value(
                        contactRepository.getReferenceById(UUID.fromString(testTaskReference.getContactId())).getCompany().getName()))
                .andExpect(jsonPath("content.[0].TaskCustomField1").value("task custom field 1"))
                .andExpect(jsonPath("content.[0].JobCustomList1").value("job custom list 1"))
                .andExpect(jsonPath("content.[0].ContactCustomField4").value("contact custom field 4"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("creating task test")
    @Transactional
    void createTask() throws Exception {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder()
                .customField1("test custom field1")
                .customField2("test custom field2")
                .build();
        String taskJson = objectMapper.writeValueAsString(generateTestTaskRequest());
        String taskQueryString = getQueryString(taskJson, false);
        String taskCustomQueryString = getQueryString(
                objectMapper.writeValueAsString(customFieldsAndLists), true);
        String taskReferenceQueryString = getQueryString(
                objectMapper.writeValueAsString(generateTestTaskReferenceRequest()), true);

        mockMvc.perform(post("/task" +
                             taskQueryString +
                             taskCustomQueryString +
                             taskReferenceQueryString)
                )
                .andExpect(jsonPath("$.TaskGroups").value("Test task group"))
                .andExpect(jsonPath("$.TaskCustomField1").value(customFieldsAndLists.getCustomField1()))
                .andExpect(jsonPath("$.TaskCustomField2").value(customFieldsAndLists.getCustomField2()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("update task description is correct test")
    @Transactional
    void updateTaskDescription_CorrectUpdate() throws Exception {
        Task newTask = saveAndGetTestTaskToUpdate();
        UpsertTaskRequest updatedTaskRequest = getUpdatedTaskRequestByTask(newTask);
        String updatedTaskJson = objectMapper.writeValueAsString(updatedTaskRequest);
        String updatedTaskQueryString = getQueryString(updatedTaskJson, false);
        CustomFieldsAndLists customFL = generateTestTaskCustomFieldsRequest();
        customFL.setCustomField1("new custom field 1 value");
        String updatedCustomFL = getQueryString(objectMapper.writeValueAsString(customFL), true);
        mockMvc.perform(put("/task/" + taskRepository.findAll().get(0).getId() +
                            updatedTaskQueryString +
                            updatedCustomFL)
                )
                .andExpect(jsonPath("$.TaskCustomField1").value(
                        customFL.getCustomField1()))
                .andExpect(status().isOk()
                );

        Task task = taskService.findById(newTask.getId().toString());
        assertEquals(task.getNotes(), newTask.getNotes());
        assertEquals(task.getDescription(), updatedTaskRequest.getDescription());
    }

    @Test
    @DisplayName("update task's dates by it's id is correct test")
    @Transactional
    void updateTaskDates_CorrectUpdate() throws Exception {
        Task newTask = saveAndGetTestTaskToUpdate();
        mockMvc.perform(put("/task")
                        .param("taskId", String.valueOf(taskRepository.findAll().get(0).getId()))
                        .param("Start", String.valueOf(LocalDate.now()))
                )
                .andExpect(jsonPath("content.[0].TaskId").value(
                        String.valueOf(newTask.getId())))
                .andExpect(status().isOk()
                );

        Task task = taskService.findById(newTask.getId().toString());
        assertEquals(task.getNotes(), newTask.getNotes());
        assertEquals(task.getDuration(), newTask.getDuration());
    }

    @Test
    @DisplayName("update assigning task to Unassigned contact and empty job is correct test")
    @Transactional
    void updateAssigningTaskToUnassignedContactAndEmptyJob_CorrectUpdate() throws Exception {
        Task newTask = saveAndGetTestTaskToUpdate();
        UpsertTaskRequest updatedTaskRequest = getUpdatedTaskRequestByTask(newTask);
        String updatedTaskJson = objectMapper.writeValueAsString(updatedTaskRequest);
        String updatedTaskQueryString = getQueryString(updatedTaskJson, false);
        CustomFieldsAndLists customFL = generateTestTaskCustomFieldsRequest();
        customFL.setCustomField1("new custom field 1 value");
        String updatedCustomFL = getQueryString(objectMapper.writeValueAsString(customFL), true);
        TaskReferencesRequest updatedTaskReference = TaskReferencesRequest.builder()
                .contactId("")
                .jobNumber("")
                .build();
        String updatedReference = getQueryString(objectMapper.writeValueAsString(updatedTaskReference), true);
        mockMvc.perform(put("/task/" + taskRepository.findAll().get(0).getId() +
                            updatedTaskQueryString +
                            updatedCustomFL +
                            updatedReference)
                )
                .andExpect(jsonPath("$.TaskCustomField1").value(
                        customFL.getCustomField1()))
                .andExpect(jsonPath("$.ContactId").value(
                        contactRepository.getUnassigned().orElseThrow().getId().toString()))
                .andExpect(jsonPath("$.JobNumber").value(""))
                .andExpect(status().isOk()
                );

        Task task = taskService.findById(newTask.getId().toString());
        assertEquals(task.getNotes(), newTask.getNotes());
        assertEquals(task.getDescription(), updatedTaskRequest.getDescription());
        assertNull(task.getJob());
    }

    //-------------------------UTIL-METHODS------------------------------

    private Task saveAndGetTestTaskToUpdate() {
        return saveTaskInDbAndGet(UpsertTaskRequest.builder()
                        .targetStart(LocalDate.now())
                        .duration(2)
                        .targetFinish(LocalDate.now().plusDays(2))
                        .notes("Some task notes")
                        .description("Test Task to update")
                        .actualFinish(LocalDate.now())
                        .status(TaskStatus.DONE)
                        .marked(false)
                        .build(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
    }

    private UpsertTaskRequest getUpdatedTaskRequestByTask(Task newTask) {
        String updatedDescriptionText = "task updated";
        return UpsertTaskRequest.builder()
                .description(updatedDescriptionText)
                .id(newTask.getId())
                .build();
    }

    private Task saveAndGetTestTaskToDelete() {
        return saveTaskInDbAndGet(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest()
        );
    }
}