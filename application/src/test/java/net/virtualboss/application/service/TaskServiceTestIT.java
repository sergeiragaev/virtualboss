package net.virtualboss.application.service;

import net.virtualboss.common.exception.CircularLinkingException;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.repository.ContactRepository;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskFilter;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
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
        jobRepository.deleteAll();
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
        filter.setTaskIds(Collections.singletonList(savedTaskMap.get("TaskNumber").toString()));
        filter.setJobIds(Collections.singletonList(savedTaskMap.get("JobId").toString()));
        filter.setContactIds(Collections.singletonList(savedTaskMap.get("ContactId").toString()));
        filter.setIsActive(true);
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setThisDate(LocalDate.now().plusDays(5));
        List<Map<String, Object>> result = taskService.findAll(null, filter);
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
        String savedTaskId = savedTaskMap.get("TaskNumber").toString();
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
    @DisplayName("Create 10 pending tasks and compare follows")
    @Transactional
    void create10PendingTasksAndCompareFollows() {
        Map<Long, Map<String, Object>> tasks = create10PendingTasks();
        long parentTaskNumber = taskRepository.findAll().stream().map(Task::getNumber).min(Long::compareTo).orElseThrow();
        String parentTasks = "";
        for (long i = parentTaskNumber + 1; i <= parentTaskNumber + 10; i++) {
            parentTasks = parentTasks.isBlank() ? String.valueOf(i - 1) : parentTasks + "," + (i - 1);
            Map<String, Object> currentTask = tasks.get(i);
            Task task = taskService.getTaskById(currentTask.get("TaskId").toString());
            assertEquals(task.getTargetStart(),
                    currentTask.get("TaskTargetStart")
            );
            assertEquals(task.getTargetFinish(),
                    currentTask.get("TaskTargetFinish")
            );
            assertEquals(parentTasks, currentTask.get("TaskFollows"));
        }
    }


    @Test
    @DisplayName("Create 2 pending sequential tasks and trying to link first with last one")
    @Transactional
    void create2PendingSequentialTasksAndTryingToLinkFirstWithLastOne() {
        create2PendingSequentialTasks();
        long firstTaskNumber = taskRepository.findAll().stream().map(Task::getNumber).min(Long::compareTo).orElseThrow();
        String firstTaskId = taskRepository.findByNumber(firstTaskNumber).orElseThrow().getId().toString();
        UpsertTaskRequest upsertRequest = UpsertTaskRequest.builder().build();
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();
        String pendingRef = String.valueOf(firstTaskNumber + 2);
        TaskReferencesRequest taskRefRequest = TaskReferencesRequest.builder().pending(pendingRef).build();
        assertThrows(CircularLinkingException.class,
                () -> taskService.saveTask(firstTaskId, upsertRequest, customFieldsAndLists, taskRefRequest)
        );
    }

    @Test
    @DisplayName("Create 2 pending sequential tasks and changing first task dates")
    @Transactional
    void create2PendingSequentialTasksAndChangingFirstTaskDates() {
        create2PendingSequentialTasks();

        long firstTaskNumber = taskRepository.findAll().stream().map(Task::getNumber).min(Long::compareTo).orElseThrow();
        Task firstTask = taskRepository.findByNumber(firstTaskNumber).orElseThrow();
        int oldFirstTaskDuration = firstTask.getDuration();
        LocalDate oldFirstTaskStart = firstTask.getTargetStart();

        Task lastTask = taskRepository.findByNumber(firstTaskNumber + 2).orElseThrow();
        LocalDate oldLastTaskFinish = lastTask.getTargetFinish();
        int oldLastTaskDuration = lastTask.getDuration();

        taskService.updateTaskByStartAndFinish(firstTask.getId().toString(),
                firstTask.getTargetStart().plusDays(5), null);
        assertNotEquals(oldFirstTaskStart, firstTask.getTargetStart());
        assertNotEquals(oldFirstTaskDuration, firstTask.getDuration());

        entityManager.flush();
        entityManager.clear();

        taskService.updateTaskByStartAndFinish(firstTask.getId().toString(),
                null, firstTask.getTargetFinish().plusDays(5));

        assertNotEquals(oldLastTaskFinish, taskRepository.findByNumber(firstTaskNumber + 2).orElseThrow().getTargetFinish());
        assertEquals(oldLastTaskDuration, lastTask.getDuration());
    }
}
