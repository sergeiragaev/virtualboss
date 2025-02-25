package net.virtualboss.application.service;

import net.virtualboss.common.exception.AlreadyExistsException;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.job.service.JobService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import net.virtualboss.job.web.dto.UpsertJobRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobServiceTestIT extends TestDependenciesContainer {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JobService jobService;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearAllRepositories();
    }

    @Test
    @DisplayName("Get job by ID returns valid job")
    @Transactional
    void getJobById_ReturnsValidJob() {
        UpsertJobRequest request = generateTestJobRequest();
        CustomFieldsAndLists customFieldsAndLists = generateTestJobCustomFieldsRequest();
        Map<String, Object> savedJob = jobService.createJob(request, customFieldsAndLists);
        Job result = jobService.getJobById(savedJob.get("JobId").toString());
        assertEquals(savedJob.get("JobId"), result.getId());
        assertEquals(request.getNumber(), result.getNumber());
        assertEquals(customFieldsAndLists.getCustomField2(),
                result.getCustomValueByName("JobCustomField2"));
    }

    @Test
    @DisplayName("Create new job with duplicated job number")
    @Transactional
    void createNewJobWithDuplicatedJobNumber() {
        UpsertJobRequest request = generateTestJobRequest();
        CustomFieldsAndLists customFL = generateTestJobCustomFieldsRequest();
        jobService.createJob(request, customFL);
        assertEquals(1, jobRepository.count());
        assertThrows(AlreadyExistsException.class,
                () -> jobService.createJob(request, customFL));
        assertEquals(1, jobRepository.count());
    }

    @Test
    @DisplayName("Update job failed trying set duplicated job number")
    @Transactional
    void updateNewJobWithDuplicatedJobNumber() {
        UpsertJobRequest request = generateTestJobRequest();
        CustomFieldsAndLists customFL = generateTestJobCustomFieldsRequest();
        Map<String, Object> firstJob = jobService.createJob(request, customFL);
        request.setNumber("New job number");
        Map<String, Object> secondJob = jobService.createJob(request, customFL);
        assertEquals(2, jobRepository.count());
        request.setNumber(firstJob.get("JobNumber").toString());
        String jobId = secondJob.get("JobId").toString();
        assertThrows(AlreadyExistsException.class, () -> jobService.saveJob(jobId, request, null));
        assertEquals(2, jobRepository.count());
    }

    @Test
    @DisplayName("Update job correctly wile trying to set duplicated deleted job number ")
    @Transactional
    void updateNewJobWithDeletedDuplicatedJobNumber() {
        UpsertJobRequest request = generateTestJobRequest();
        CustomFieldsAndLists customFL = generateTestJobCustomFieldsRequest();
        Map<String, Object> firstJob = jobService.createJob(request, customFL);
        request.setNumber("New job number");
        Map<String, Object> secondJob = jobService.createJob(request, customFL);
        assertEquals(2, jobRepository.count());
        request.setNumber(firstJob.get("JobNumber").toString());
        jobService.deleteJob(firstJob.get("JobId").toString());
        assertDoesNotThrow(
                () -> jobService.saveJob(secondJob.get("JobId").toString(), request, null));
        assertEquals(2, jobRepository.count());
    }

    @Test
    @DisplayName("Update job correctly")
    @Transactional
    void updateJob_CorrectUpdate() {
        CustomFieldsAndLists customFieldsAndLists = generateTestJobCustomFieldsRequest();
        Map<String, Object> savedJob = jobService.createJob(
                generateTestJobRequest(), customFieldsAndLists);
        String id = savedJob.get("JobId").toString();
        UpsertJobRequest updatedRequest = UpsertJobRequest.builder()
                .id(UUID.fromString(id))
                .number("Updated job number")
                .build();
        customFieldsAndLists.setCustomList1("Updated Job custom list1");
        jobService.saveJob(id, updatedRequest, customFieldsAndLists);
        Job updatedJob = jobRepository.findById(UUID.fromString(id)).orElseThrow();
        assertEquals("Updated job number", updatedJob.getNumber());
        assertEquals("Updated Job custom list1", updatedJob.getCustomValueByName("JobCustomList1"));
        assertEquals("job custom field 2", updatedJob.getCustomValueByName("JobCustomField2"));
    }

    @Test
    @DisplayName("Delete job correctly")
    @Transactional
    void deleteJob_CorrectDelete() {
        Map<String, Object> savedTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        String taskId = savedTask.get("TaskId").toString();
        String jobId = savedTask.get("JobId").toString();
        Job taskJob = jobRepository.findById(UUID.fromString(jobId)).orElseThrow();
        assertEquals(taskRepository.findById(UUID.fromString(taskId)).orElseThrow().getJob(), taskJob);
        jobService.deleteJob(jobId);
        assertTrue(jobRepository.findById(UUID.fromString(jobId)).orElseThrow().getIsDeleted());
        assertNull(taskRepository.findById(UUID.fromString(taskId)).orElseThrow().getJob());
    }

    @Test
    @DisplayName("Search jobs with specific word in custom fields")
    @Transactional
    void searchJobs() {
        jobService.createJob(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        filter.setFindString("custom");
        List<Map<String, Object>> result = jobService.findAll("JobId", filter);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Search specific job by filters")
    @Transactional
    void searchSpecificJobByFilters() {
        Map<String, Object> savedJobMap = jobService.createJob(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        String savedJobNumber = savedJobMap.get("JobNumber").toString();
        String savedJobId = savedJobMap.get("JobId").toString();
        filter.setFindString(savedJobNumber);
        filter.setIsDeleted(false);
        List<Map<String, Object>> result = jobService.findAll("JobId", filter);
        assertNotNull(result);
        assertFalse(result.get(0).isEmpty());
        assertEquals(1, result.size());
        assertEquals(savedJobId, result.get(0).get("JobId").toString());
    }

    @Test
    @DisplayName("Search job with non-matching filters")
    @Transactional
    void searchJobWithNonMatchingFilters() {
        Map<String, Object> savedJobMap = jobService.createJob(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        CommonFilter filter = new CommonFilter();
        String savedJobId = savedJobMap.get("JobId").toString();
        filter.setIsDeleted(false);
        filter.setFindString(savedJobId);
        List<Map<String, Object>> result = jobService.findAll("JobId", filter);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Create new job")
    @Transactional
    void createNewJob() {
        jobService.createJob(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        assertEquals(1, jobRepository.count());
    }
}
