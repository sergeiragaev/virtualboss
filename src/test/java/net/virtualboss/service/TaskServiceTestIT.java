package net.virtualboss.service;

import net.virtualboss.TestDependenciesContainer;
import net.virtualboss.model.entity.Task;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.TaskFilter;
import net.virtualboss.web.dto.task.TaskReferencesRequest;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskServiceTestIT extends TestDependenciesContainer {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ContactRepository contactRepository;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Get task by ID returns valid task")
    @Transactional
    void getTaskById_ReturnsValidTask() {
        UpsertTaskRequest request = generateTestTaskRequest();
        CustomFieldsAndLists customFieldsAndLists = generateTestTaskCustomFieldsRequest();
        Map<String, Object> savedTask = taskService.createNewTask(
                request,
                customFieldsAndLists,
                generateTestTaskReferenceRequest());
        Task result = taskService.getTaskById(savedTask.get("TaskId").toString());
        assertEquals(savedTask.get("TaskId"), result.getId());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(customFieldsAndLists.getCustomField1(),
                result.getCustomValueByName("TaskCustomField1"));
    }

    @Test
    @DisplayName("Update task correctly")
    @Transactional
    void updateTask_CorrectUpdate() {
        Map<String, Object> savedTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest()
        );
        String taskId = savedTask.get("TaskId").toString();
        UpsertTaskRequest updatedTaskRequest = UpsertTaskRequest.builder()
                .id(UUID.fromString(taskId))
                .description("Updated task description")
                .build();
        TaskReferencesRequest referenceRequest = TaskReferencesRequest.builder()
                .contactId("")
                .jobNumber("")
                .build();
        taskService.saveTask(
                taskId, updatedTaskRequest,
                CustomFieldsAndLists.builder().build(),
                referenceRequest
        );
        Task updatedTask = taskRepository.findById(UUID.fromString(taskId)).orElseThrow();
        assertEquals("Updated task description", updatedTask.getDescription());
        assertEquals(contactRepository.getUnassigned().orElseThrow(), updatedTask.getContact());
        assertNull(updatedTask.getJob());
    }

    @Test
    @DisplayName("Delete task correctly")
    @Transactional
    void deleteTask_CorrectDelete() {
        Map<String, Object> savedTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        String taskId = savedTask.get("TaskId").toString();
        taskService.deleteTaskById(taskId);
        assertTrue(taskRepository.findById(UUID.fromString(taskId)).orElseThrow().getIsDeleted());
    }

    @Test
    @DisplayName("Search tasks with custom fields values")
    @Transactional
    void searchTasksWithCustomFieldsValues() {
        taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        TaskFilter filter = new TaskFilter();
        filter.setFindString("custom");
        List<Map<String, Object>> result = taskService.findAll("TaskId", filter);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Search specific task by filters")
    @Transactional
    void searchSpecificTaskByFilters() {
        Map<String, Object> savedTaskMap = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        TaskFilter filter = new TaskFilter();
        String savedTaskId = savedTaskMap.get("TaskId").toString();
        filter.setTaskIds(Collections.singletonList(savedTaskId));
        filter.setJobIds(Collections.singletonList(savedTaskMap.get("JobId").toString()));
        filter.setContactIds(Collections.singletonList(savedTaskMap.get("ContactId").toString()));
        filter.setIsActive(true);
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setThisDate(LocalDate.now());
        List<Map<String, Object>> result = taskService.findAll("TaskId", filter);
        assertNotNull(result);
        assertFalse(result.get(0).isEmpty());
        assertEquals(1, result.size());
        assertEquals(savedTaskId, result.get(0).get("TaskId").toString());
    }

    @Test
    @DisplayName("Search task with non-matching filters")
    @Transactional
    void searchTaskWithNonMatchingFilters() {
        Map<String, Object> savedTaskMap = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        TaskFilter filter = new TaskFilter();
        String savedTaskId = savedTaskMap.get("TaskId").toString();
        filter.setTaskIds(Collections.singletonList(savedTaskId));
        filter.setJobIds(Collections.singletonList(savedTaskMap.get("JobId").toString()));
        filter.setContactIds(Collections.singletonList(savedTaskMap.get("ContactId").toString()));
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setThisDate(LocalDate.now().minusDays(10));
        List<Map<String, Object>> result = taskService.findAll("TaskId", filter);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Create new task")
    @Transactional
    void createNewTask() {
        taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        assertEquals(1, taskRepository.count());
    }

    @Test
    @DisplayName("Create 2 pending tasks")
    @Transactional
    void create2PendingTasks() {
        Map<String, Object> savedTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        assertEquals(1, taskRepository.count());
        String parentTaskNumber = savedTask.get("TaskNumber").toString();
        Map<String, Object> pendingTask1 = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                TaskReferencesRequest.builder().pending(parentTaskNumber).build());
        assertEquals(2, taskRepository.count());
        assertEquals(pendingTask1.get("TaskFollows"), parentTaskNumber);
        String parentTasks = parentTaskNumber + "," + pendingTask1.get("TaskNumber").toString();
        Map<String, Object> pendingTask2 = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                TaskReferencesRequest.builder().pending(parentTasks).build());
        assertEquals(3, taskRepository.count());
        assertEquals(pendingTask2.get("TaskFollows"), parentTasks);
    }
}
