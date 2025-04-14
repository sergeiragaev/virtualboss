package net.virtualboss.application.web.controller.v1;

import net.virtualboss.application.service.TestDependenciesContainer;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.job.service.JobService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.job.web.dto.UpsertJobRequest;
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
class JobControllerIT extends TestDependenciesContainer {
    @Autowired
    private JobService jobService;

    @BeforeEach
    void initBeforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        clearAllRepositories();
    }

    @Test
    @DisplayName("test get job by id")
    void getJobById_ReturnsValidJob() throws Exception {
        Job job = saveJobInDbAndGet(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        String customValue = job.getCustomValueByName("JobCustomField2");
        mockMvc.perform(get("/job/" + jobRepository.findAll().get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.JobNumber").value(job.getNumber()))
                .andExpect(jsonPath("$.JobCustomField2")
                        .value(customValue))
                .andReturn();
    }

    @Test
    @DisplayName("job successfully deleted test")
    void deleteJobById_CorrectDelete() throws Exception {
        Job job = saveAndGetTestJobToDelete();
        mockMvc.perform(delete("/job/" + job.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("job deletion failed of fake id test")
    void deleteJobById_NotFound() throws Exception {
        Job job = saveAndGetTestJobToDelete();
        job.setId(UUID.randomUUID());
        mockMvc.perform(delete("/job/" + job.getId())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("creating job test")
    void createJob() throws Exception {
        CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder()
                .customField1("test custom field1")
                .customField2("test custom field2")
                .build();
        String jsonString = objectMapper.writeValueAsString(generateTestJobRequest());
        String queryString = getQueryString(jsonString, false);
        String customQueryString = getQueryString(
                objectMapper.writeValueAsString(customFieldsAndLists), true);

        mockMvc.perform(post("/job" + queryString + customQueryString)
//                        .header("id", 1L)
                )
                .andExpect(jsonPath("$.JobGroups").value("Test job group"))
                .andExpect(jsonPath("$.JobCustomField1").value(customFieldsAndLists.getCustomField1()))
                .andExpect(jsonPath("$.JobCustomField2").value(customFieldsAndLists.getCustomField2()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("update job number and custom field1 value is correct test")
    void updateJobNumberById_CorrectUpdate() throws Exception {
        Job newJob = saveAndGetTestJobToUpdate();
        UpsertJobRequest updatedRequest = getUpdatedJobRequestByJob(newJob);
        String updatedJson = objectMapper.writeValueAsString(updatedRequest);
        String updatedQueryString = getQueryString(updatedJson, false);
        CustomFieldsAndLists customFL = generateTestJobCustomFieldsRequest();
        customFL.setCustomField1("new job custom field 1 value");
        String updatedCustomFL = getQueryString(objectMapper.writeValueAsString(customFL), true);
        mockMvc.perform(put("/job/" + jobRepository.findAll().get(0).getId() +
                                updatedQueryString +
                                updatedCustomFL)
//                        .header("id", 1L)
                )
                .andExpect(jsonPath("$.JobCustomField1").value(
                        customFL.getCustomField1()))
                .andExpect(jsonPath("$.JobNumber").value(
                        updatedRequest.getNumber()))
                .andExpect(status().isOk()
                );
    }

    @Test
    @DisplayName("search jobs with specific criteria api test")
    void searchJobs() throws Exception {
        UpsertJobRequest testRequest = generateTestJobRequest();
        saveJobInDbAndGet(testRequest, generateTestJobCustomFieldsRequest());
        mockMvc.perform(get("/job")
                        .param("fields", "JobId,JobNumber,JobCustomField2")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(10))
                        .param("sort", "number:asc")
                        .param("findString", "custom field")
                )
                .andExpect(jsonPath("content.[0].JobNumber").value(testRequest.getNumber()))
                .andExpect(jsonPath("content.[0].JobCustomField2").value("job custom field 2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("search deleted job test")
    void searchDeletedJob() throws Exception {
        UpsertJobRequest testRequest = generateTestJobRequest();
        Job job = saveJobInDbAndGet(testRequest, generateTestJobCustomFieldsRequest());
        jobService.deleteJob(job.getId().toString());
        mockMvc.perform(get("/job")
                        .param("fields", "JobId,JobNumber")
                        .param("isDeleted", String.valueOf(true))
                        .param("findString", "custom list")
                )
                .andExpect(jsonPath("content.[0].JobNumber").value(testRequest.getNumber()))
                .andExpect(status().isOk());
    }

    //-------------------------UTIL-METHODS------------------------------

    private Job saveAndGetTestJobToUpdate() {
        return saveJobInDbAndGet(UpsertJobRequest.builder()
                        .number("new job number")
                        .postal("new postal")
                        .build(),
                generateTestJobCustomFieldsRequest());
    }

    private UpsertJobRequest getUpdatedJobRequestByJob(Job newJob) {
        String updatedJobNumber = "updated job number";
        return UpsertJobRequest.builder()
                .number(updatedJobNumber)
                .id(newJob.getId())
                .build();
    }

    private Job saveAndGetTestJobToDelete() {
        return saveTaskInDbAndGet(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest()).getJob();
    }
}