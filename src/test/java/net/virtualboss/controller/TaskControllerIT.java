package net.virtualboss.controller;

import net.virtualboss.TestDependenciesContainer;
import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.DateCriteria;
import net.virtualboss.model.enums.DateRange;
import net.virtualboss.model.enums.DateType;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void getTaskById_ReturnsValidTask() throws Exception {
        Task task = saveTaskInDbAndGet(generateTestTaskRequest(), generateTestCustomFieldsRequest());
        String customValue = task.getCustomValueByName("TaskCustomField1");
        mockMvc.perform(get("/task/" + taskRepository.findAll().get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TaskDescription").value(task.getDescription()))
                .andExpect(jsonPath("$.TaskCustomField1")
                        .value(customValue))
                .andReturn();
    }

    //    @Test
//    @DisplayName("update post is forbidden of fake author test")
//    void updatePostById_NotAccess() throws Exception {
//        Post newPost = saveAndGetTestPostToUpdate();
//        PostDto updatedPostDto = getUpdatedPostDtoByPost(newPost);
//        String updatedPostJson = objectMapper.writeValueAsString(updatedPostDto);
//        mockMvc.perform(put(apiPrefix + "/post")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updatedPostJson)
//                        .header("id", 2L))
//                .andExpect(status().isForbidden());
//    }
//
    @Test
    @DisplayName("task successfully deleted test")
    void deleteTaskById_CorrectDelete() throws Exception {
        Task task = saveAndGetTestTaskToDelete();
        mockMvc.perform(delete("/task/" + task.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("task deletion failed of fake id test")
    void deletePostById_NotAccess() throws Exception {
        Task task = saveAndGetTestTaskToDelete();
        task.setId(UUID.randomUUID());
        mockMvc.perform(delete("/task/" + task.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("search tasks with specific criteria api test")
    void searchTasks() throws Exception {
        UpsertTaskRequest testTaskRequest = generateTestTaskRequest();
        saveTaskInDbAndGet(testTaskRequest, generateTestCustomFieldsRequest());
        mockMvc.perform(get("/task")
                        .param("fields", "TaskId,TaskDescription")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(10))
                        .param("sort", "id,asc")
                        .param("isActive", String.valueOf(true))
                        .param("dateType", String.valueOf(DateType.TARGET_START.getValue()))
                        .param("isDateRange", String.valueOf(true))
                        .param("dateRange", String.valueOf(DateRange.DATE_PERIOD.getValue()))
                        .param("dateFrom", LocalDate.now().toString())
                        .param("dateTo", LocalDate.now().plusDays(1).toString())
                        .param("dateCriteria", String.valueOf(DateCriteria.EXACT.getValue()))
                        .param("jobIds", String.valueOf(
                                jobRepository.findByNumberIgnoreCase(testTaskRequest.getJobNumber()).orElseThrow().getId()))
                        .param("custIds", String.valueOf(testTaskRequest.getContactId()))
                        .param("isDeleted", String.valueOf(false))
                        .param("findString", "task custom field 6")
                )
                .andExpect(jsonPath("[0].TaskDescription").value(testTaskRequest.getDescription()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("creating task test")
    void createTask() throws Exception {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder()
                .customField1("test custom field1")
                .customField2("test custom field2")
                .build();
        String taskJson = objectMapper.writeValueAsString(generateTestTaskRequest());
        String taskQueryString = getQueryString(taskJson, false);
        String taskCustomQueryString = getQueryString(
                objectMapper.writeValueAsString(customFieldsAndLists), true);

        mockMvc.perform(post("/task" + taskQueryString + taskCustomQueryString)
//                        .header("id", 1L)
                )
                .andExpect(jsonPath("$.TaskGroups").value("Test task group"))
                .andExpect(jsonPath("$.TaskCustomField1").value(customFieldsAndLists.getCustomField1()))
                .andExpect(jsonPath("$.TaskCustomField2").value(customFieldsAndLists.getCustomField2()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("update task description is correct test")
    void updateTaskDescriptionById_CorrectUpdate() throws Exception {
        Task newTask = saveAndGetTestTaskToUpdate();
        UpsertTaskRequest updatedTaskRequest = getUpdatedTaskRequestByTask(newTask);
        String updatedTaskJson = objectMapper.writeValueAsString(updatedTaskRequest);
        String updatedTaskQueryString = getQueryString(updatedTaskJson, false);
        CustomFieldsAndLists customFL = generateTestCustomFieldsRequest();
        customFL.setCustomField1("new custom field 1 value");
        String updatedCustomFL = getQueryString(objectMapper.writeValueAsString(customFL), true);
        mockMvc.perform(put("/task/" + taskRepository.findAll().get(0).getId() +
                                updatedTaskQueryString +
                                updatedCustomFL)
//                        .header("id", 1L)
                )
                .andExpect(jsonPath("$.TaskCustomField1").value(
                        customFL.getCustomField1()))
                .andExpect(status().isOk()
                );

        Task task = taskService.getTaskById(newTask.getId().toString());
        assertEquals(task.getNotes(), newTask.getNotes());
        assertEquals(task.getDescription(), updatedTaskRequest.getDescription());
    }

    //-------------------------UTIL-METHODS------------------------------

    private Task saveAndGetTestTaskToUpdate() {
        return saveTaskInDbAndGet(UpsertTaskRequest.builder()
                        .targetStart(LocalDate.now())
                        .duration((short) 2)
                        .targetFinish(LocalDate.now().plusDays(2))
                        .notes("Some task notes")
                        .description("Test Task to update")
                        .actualFinish(LocalDate.now())
                        .status("Done")
                        .isDeleted(false)
                        .build(),
                generateTestCustomFieldsRequest());
    }

    private UpsertTaskRequest getUpdatedTaskRequestByTask(Task newTask) {
        String updatedDescriptionText = "task updated";
        return UpsertTaskRequest.builder()
                .description(updatedDescriptionText)
                .id(newTask.getId())
                .build();
    }

    private Task saveAndGetTestTaskToDelete() {
        return saveTaskInDbAndGet(generateTestTaskRequest(), generateTestCustomFieldsRequest());
    }
}