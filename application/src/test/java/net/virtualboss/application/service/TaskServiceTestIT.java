package net.virtualboss.application.service;

import net.virtualboss.common.exception.CircularLinkingException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.entity.Holiday;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.model.enums.DateCriteria;
import net.virtualboss.common.model.enums.DateRange;
import net.virtualboss.common.model.enums.DateType;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskFilter;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskServiceTestIT extends TestDependenciesContainer {

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        taskRepository.deleteAll();
        jobRepository.deleteAll();
        holidayRepository.deleteAll();
        Holiday holiday = new Holiday();
        holiday.setCountryCode("US");
        holiday.setDate(LocalDate.now().minusDays(1));
        holiday.setName("test");
        holiday.setRecurring(false);
        holidayRepository.save(holiday);
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
        Task result = taskService.findById(savedTask.get("TaskId").toString());
        assertEquals(savedTask.get("TaskId"), result.getId());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getDuration(), result.getDuration());
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
        Page<Map<String, Object>> result = taskService.findAll("TaskId", filter);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
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
        filter.setIsDone(true);
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setThisDate(LocalDate.now().plusDays(5));
        Page<Map<String, Object>> result = taskService.findAll(null, filter);
        assertNotNull(result);
        assertFalse(result.getContent().get(0).isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals(savedTaskId, result.getContent().get(0).get("TaskId").toString());
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
        filter.setJobIds(Collections.singletonList(savedTaskMap.get("ContactId").toString()));
        filter.setContactIds(Collections.singletonList(savedTaskMap.get("JobId").toString()));
        String taskId = savedTaskMap.get("TaskId").toString();
        filter.setLinkingTask(taskId);
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setThisDate(LocalDate.now().minusDays(10));
        Page<Map<String, Object>> result = taskService.findAll("TaskId", filter);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Search task with date filters")
    @Transactional
    void searchTaskWithDateFilters() {
        taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        TaskFilter filter = new TaskFilter();
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setDateRange(DateRange.DATE_PERIOD.getValue());
        filter.setDateFrom(LocalDate.now().minusDays(365));
        filter.setDateTo(LocalDate.now().minusDays(360));
        filter.setDateCriteria(DateCriteria.ON_OR_BEFORE.getValue());
        filter.setDateType(DateType.TARGET_FINISH.getValue());
        Page<Map<String, Object>> result = taskService.findAll("ContactPerson", filter);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Search task with today's date filters")
    @Transactional
    void searchTaskWithTodayDateFilters() {
        taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        TaskFilter filter = new TaskFilter();
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setDateCriteria(DateCriteria.EXACT.getValue());
        filter.setDateType(DateType.ACTUAL_FINISH.getValue());
        filter.setDateRange(DateRange.DATE_PERIOD.getValue());
        filter.setDateFrom(LocalDate.now().minusDays(2));
        filter.setDateTo(LocalDate.now().minusDays(1));
        Page<Map<String, Object>> result = taskService.findAll(null, filter);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Search specific task by date filters")
    @Transactional
    void searchSpecificTaskByDateFilters() {
        Map<String, Object> savedTaskMap = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        TaskFilter filter = new TaskFilter();
        String savedTaskId = savedTaskMap.get("TaskId").toString();
        filter.setIsActive(true);
        filter.setIsDone(true);
        filter.setIsDateRange(true);
        filter.setIsDeleted(false);
        filter.setDateType(DateType.ANY_DATE_FIELD.getValue());
        filter.setDateCriteria(DateCriteria.ON_OR_AFTER.getValue());
        Page<Map<String, Object>> result = taskService.findAll(null, filter);
        assertNotNull(result);
        assertFalse(result.getContent().get(0).isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(savedTaskId, result.getContent().get(0).get("TaskId").toString());
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
            Task task = taskService.findById(currentTask.get("TaskId").toString());
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
    @DisplayName("create pending task and trying to link with non existent")
    @Transactional
    void createPendingTaskAndTryingToLinkWithNonExistent() {
        create2PendingSequentialTasks();
        long firstTaskNumber = taskRepository.findAll().stream().map(Task::getNumber).min(Long::compareTo).orElseThrow();
        String firstTaskId = taskRepository.findByNumber(firstTaskNumber).orElseThrow().getId().toString();
        UpsertTaskRequest upsertRequest = UpsertTaskRequest.builder().build();
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();
        String pendingRef = String.valueOf(firstTaskNumber + 4);
        TaskReferencesRequest taskRefRequest = TaskReferencesRequest.builder().pending(pendingRef).build();
        assertThrows(EntityNotFoundException.class,
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
                firstTask.getTargetStart().minusDays(10), null);
        assertNotEquals(oldFirstTaskStart, firstTask.getTargetStart());
        assertNotEquals(oldFirstTaskDuration, firstTask.getDuration());

        entityManager.flush();
        entityManager.clear();

        taskService.updateTaskByStartAndFinish(firstTask.getId().toString(),
                null, firstTask.getTargetFinish().plusDays(25));
        if (firstTask.getStatus() == TaskStatus.ACTIVE) {
            assertNotEquals(oldLastTaskFinish, taskRepository.findByNumber(firstTaskNumber + 2).orElseThrow().getTargetFinish());
        }
        assertEquals(oldLastTaskDuration, lastTask.getDuration());
    }

    @Test
    @DisplayName("Find all tasks with nested entity custom fields")
    @Transactional
    void findAllTasksWithNestedEntityCustomFields() {
        create2PendingSequentialTasks();
        Page<Map<String, Object>> response = taskService.findAll(
                "JobLot,TaskDescription,TaskTargetStart,JobNumber,TaskCustomField2,JobCustomList1,ContactPerson",
                new TaskFilter());
        long firstTaskNumber = taskRepository.findAll().stream().map(Task::getNumber).min(Long::compareTo).orElseThrow();
        Task firstTask = taskRepository.findByNumber(firstTaskNumber).orElseThrow();
        assertEquals(firstTask.getId(), response.getContent().get(0).get("TaskId"));
        assertEquals(firstTask.getDescription(), response.getContent().get(0).get("TaskDescription"));
        assertEquals(firstTask.getTargetStart(), response.getContent().get(0).get("TaskTargetStart"));
        assertEquals(firstTask.getCustomValueByName("TaskCustomField2"), response.getContent().get(0).get("TaskCustomField2"));
        assertEquals(firstTask.getJob().getCustomValueByName("JobCustomList1"), response.getContent().get(0).get("JobCustomList1"));
        assertEquals(firstTask.getJob().getLot(), response.getContent().get(0).get("JobLot"));
        assertEquals(firstTask.getContact().getPerson(), response.getContent().get(0).get("ContactPerson"));
    }

}
