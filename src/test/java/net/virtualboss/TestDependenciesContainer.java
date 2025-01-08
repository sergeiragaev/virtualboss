package net.virtualboss;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.virtualboss.mapper.v1.ContactMapperV1;
import net.virtualboss.mapper.v1.job.JobMapperV1;
import net.virtualboss.mapper.v1.task.TaskMapperV1;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Group;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.entity.Task;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.repository.*;
import net.virtualboss.service.TaskService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.contact.UpsertContactRequest;
import net.virtualboss.web.dto.job.UpsertJobRequest;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.Iterator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestDependenciesContainer {
    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected ContactRepository contactRepository;
    @Autowired
    protected EmployeeRepository employeeRepository;
    @Autowired
    protected FieldValueRepository fieldValueRepository;
    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected TaskMapperV1 taskMapper;
    @Autowired
    protected JobMapperV1 jobMapper;
    @Autowired
    protected ContactMapperV1 contactMapper;
    @Autowired
    protected TaskService taskService;

    protected MockMvc mockMvc;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
//        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    protected Task saveTaskInDbAndGet(UpsertTaskRequest request,
                                      CustomFieldsAndLists customFieldsAndLists) {
        Task task = taskMapper.requestToTask(request, customFieldsAndLists);
        task.setNumber(taskService.getNextNumberSequenceValue());
        return taskRepository.save(task);
    }

    protected Job saveJobInDbAndGet(UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Job job = jobMapper.requestToJob(request, customFieldsAndLists);
        return jobRepository.save(job);
    }

    protected Contact saveContactInDbAndGet(UpsertContactRequest request) {
        Contact contact = contactMapper.requestToContact(request);
        return contactRepository.save(contact);
    }

    protected Group saveTaskGroupInDbAndGet() {
        Group group = Group.builder()
                .type(EntityType.TASK)
                .name("Test task group")
                .description("Test task group description")
                .build();
        return groupRepository.save(group);
    }

    protected Group saveJobGroupInDbAndGet() {
        Group group = Group.builder()
                .type(EntityType.JOB)
                .name("Test job group")
                .description("Test job group description")
                .build();
        return groupRepository.save(group);
    }

    protected UpsertTaskRequest generateTestTaskRequest() {
        Job job = saveJobInDbAndGet(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        Contact contact = saveContactInDbAndGet(generateTestContactRequest());
        Group group = saveTaskGroupInDbAndGet();
        return UpsertTaskRequest.builder()
//                .requested("Admin")
                .order("100")
                .notes("Task notes")
                .duration((short) 1)
                .description("Test task")
                .marked(true)
                .jobNumber(job.getNumber())
                .contactId(String.valueOf(contact.getId()))
                .status("Active")
                .targetStart(LocalDate.now())
                .targetFinish(LocalDate.now().plusDays(1))
                .isDeleted(false)
                .groups(String.valueOf(group.getId()))
                .build();
    }

    protected CustomFieldsAndLists generateTestTaskCustomFieldsRequest() {
        return CustomFieldsAndLists.builder()
                .customField1("task custom field 1")
                .customField6("task custom field 6")
                .customList3("task custom list 3")
                .customList5("task custom list 5")
                .build();
    }

    protected CustomFieldsAndLists generateTestJobCustomFieldsRequest() {
        return CustomFieldsAndLists.builder()
                .customField2("job custom field 2")
                .customField5("job custom field 5")
                .customList1("job custom list 1")
                .customList4("job custom list 4")
                .build();
    }

    protected UpsertContactRequest generateTestContactRequest() {
        return UpsertContactRequest.builder()
                .comments("Some comments")
                .fax("Fax #")
                .firstName("First name")
                .lastName("Last name")
                .notes("Contact notes")
                .taxId("Tax ID")
                .email("contact@email.com")
                .spouse("Spouse")
                .profession("Profession")
                .supervisor("Supervisor")
                .webSite("www.contact.org")
                .company("Contact company")
                .insuranceDate(LocalDate.now().plusYears(2))
                .workersCompDate(LocalDate.now().plusYears(1))
                .phones("Contact phones #")
                .build();
    }

    protected UpsertJobRequest generateTestJobRequest() {
        Group group = saveJobGroupInDbAndGet();
        return UpsertJobRequest.builder()
                .address1("Address first row")
                .address2("Address second row")
                .city("City")
                .state("State")
                .email("job@email.com")
                .fax("Fax number")
                .lot("Lot number")
                .company("Job Company")
                .country("Country")
                .cellPhone("Cellphone number")
                .directions("Directions to job")
                .lockBox("Loc box number")
                .notes("Job notes")
                .homePhone("Home phone number")
                .postal("zip")
                .ownerName("Owner name")
                .subdivision("Subdivision")
                .number("Some job")
                .workPhone("Work phone number")
                .groups(String.valueOf(group.getId()))
                .build();
    }

    protected void clearAllRepositories() {
        fieldValueRepository.deleteAll();
        taskRepository.deleteAll();
        jobRepository.deleteAll();
        contactRepository.deleteAll();
        employeeRepository.deleteAll();
        groupRepository.deleteAll();
    }

    protected String getQueryString(String unparsedString,
                                    boolean noQuestionMark) {
        StringBuilder sb = new StringBuilder();
        JSONObject json;
        try {
            json = new JSONObject(unparsedString);
            Iterator<String> keys = json.keys();
            if (!noQuestionMark) {
                sb.append("?"); //start of query args
            }
            while (keys.hasNext()) {
                String key = keys.next();
                if (json.get(key) == JSONObject.NULL) continue;
                sb.append(key);
                sb.append("=");
                sb.append(json.get(key));
                sb.append("&"); //To allow for another argument.
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
