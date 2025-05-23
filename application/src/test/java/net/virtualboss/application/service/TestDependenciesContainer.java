package net.virtualboss.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.repository.*;
import net.virtualboss.contact.mapper.v1.ContactMapperV1;
import net.virtualboss.contact.web.dto.ContactReferencesRequest;
import net.virtualboss.job.mapper.v1.JobMapperV1;
import net.virtualboss.task.mapper.v1.TaskMapperV1;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.task.service.TaskService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import net.virtualboss.job.web.dto.UpsertJobRequest;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestDependenciesContainer {
    @PersistenceContext
    protected EntityManager entityManager;
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
    @Autowired
    protected HolidayRepository holidayRepository;
    @Autowired
    protected CompanyRepository companyRepository;
    @Autowired
    protected ProfessionRepository professionRepository;

    protected MockMvc mockMvc;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
        System.setProperty("spring.data.redis.password", "");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    protected Task saveTaskInDbAndGet(
            UpsertTaskRequest request,
            CustomFieldsAndLists customFieldsAndLists,
            TaskReferencesRequest referenceRequest) {
        Task task = taskMapper.requestToTask(request, customFieldsAndLists, referenceRequest);
        task.setTargetStart(request.getTargetStart());
        task.setTargetFinish(request.getTargetFinish());
        task.setNumber(taskService.getNextTaskNumberSequenceValue());
        return taskRepository.save(task);
    }

    protected Job saveJobInDbAndGet(
            UpsertJobRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        Job job = jobMapper.requestToJob(request, customFieldsAndLists);
        return jobRepository.save(job);
    }

    @Transactional
    protected Contact saveContactInDbAndGet(
            UpsertContactRequest request, CustomFieldsAndLists customFieldsAndLists, ContactReferencesRequest referencesRequest) {
        Contact contact = contactMapper.requestToContact(request, customFieldsAndLists, referencesRequest);
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

    protected Group saveContactGroupInDbAndGet() {
        Group group = Group.builder()
                .type(EntityType.CONTACT)
                .name("Test contact group")
                .description("Test contact group description")
                .build();
        return groupRepository.save(group);
    }

    protected UpsertTaskRequest generateTestTaskRequest() {
        Random random = new Random();
        int duration = random.nextInt(10) * (random.nextInt(10) > 5 ? -1 : 1);
        int finishPlus = random.nextInt(10) * (random.nextInt(10) > 5 ? -1 : 1);
        TaskStatus status = random.nextInt(10) > 5 ? TaskStatus.ACTIVE : TaskStatus.DONE;
        return UpsertTaskRequest.builder()
                .order("100")
                .notes("Task notes")
                .duration(duration)
                .description("Test task")
                .marked(true)
                .status(status)
                .targetStart(LocalDate.now())
                .targetFinish(LocalDate.now().plusDays(duration))
                .actualFinish(status == TaskStatus.ACTIVE ? null : LocalDate.now().plusDays(duration))
                .finishPlus(finishPlus)
                .files("File://c:/virtualboss/program/vbicon.ico File:////rsn/c/virtualboss/program/vbicon.ico T\n" +
                       "File://c:/virtualboss/program/ud7007ownersmanualinenglish.pdf File:////rsn/c/virtualboss/program/ud7007ownersmanualinenglish.pdf T")
                .build();
    }

    protected TaskReferencesRequest generateTestTaskReferenceRequest() {
        Job job = saveJobInDbAndGet(generateTestJobRequest(), generateTestJobCustomFieldsRequest());
        Contact contact = saveContactInDbAndGet(
                generateTestContactRequest(),
                generateTestContactCustomFieldsRequest(),
                generateTestContactReferenceRequest());
        Group group = saveTaskGroupInDbAndGet();
        return TaskReferencesRequest.builder()
//                .requested("Admin")
                .jobNumber(job.getNumber())
                .contactId(String.valueOf(contact.getId()))
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

    protected CustomFieldsAndLists generateTestContactCustomFieldsRequest() {
        return CustomFieldsAndLists.builder()
                .customField3("contact custom field 3")
                .customField4("contact custom field 4")
                .customList5("contact custom list 5")
                .customList3("contact custom list 3")
                .build();
    }

    protected UpsertContactRequest generateTestContactRequest() {
        return UpsertContactRequest.builder()
                .comments("Some comments")
                .fax("Fax number")
                .firstName("First name")
                .lastName("Last name")
                .notes("Contact notes")
                .taxId("Tax ID")
                .email("contact@email.com")
                .spouse("Spouse")
                .supervisor("Supervisor")
                .webSite("www.contact.org")
                .insuranceDate(LocalDate.now().plusYears(2))
                .workersCompDate(LocalDate.now().plusYears(1))
                .build();
    }

    protected ContactReferencesRequest generateTestContactReferenceRequest() {
        Group group = saveContactGroupInDbAndGet();
        return ContactReferencesRequest.builder()
                .profession("Profession")
                .company("Contact company")
                .phones("Service #1: (800)546-5548," +
                        "Cellular #2: (734)544-5456," +
                        "Cellular #1: (734)545-6546," +
                        "Work #2: (419)875-5465," +
                        "Work #1: (800)546-4546")
                .addresses("Job site: 6638 Buck Creek, Holland, MI, 88423;" +
                           "Home: 6638 Buck Creek Dr., Holland, MI, 88423")
                .groups(String.valueOf(group.getId()))
                .build();
    }

    protected UpsertJobRequest generateTestJobRequest() {
        Group group = saveJobGroupInDbAndGet();
        Contact contact = saveContactInDbAndGet(
                generateTestContactRequest(),
                generateTestContactCustomFieldsRequest(),
                generateTestContactReferenceRequest());
        return UpsertJobRequest.builder()
//                .address1("Address first row")
//                .address2("Address second row")
//                .city("City")
//                .state("State")
//                .email("job@email.com")
//                .fax("Fax number")
                .lot("Lot number")
//                .company("Job Company")
//                .country("Country")
//                .cellPhone("Cellphone number")
                .directions("Directions to job")
                .lockBox("Loc box number")
                .notes("Job notes")
//                .homePhone("Home phone number")
//                .postal("zip")
                .ownerId(String.valueOf(contact.getId()))
                .subdivision("Subdivision")
                .number("Some job")
//                .workPhone("Work phone number")
                .groups(String.valueOf(group.getId()))
                .build();
    }

    protected Map<Long, Map<String, Object>> create10PendingTasks() {
        Map<Long, Map<String, Object>> tasks = new TreeMap<>();
        Map<String, Object> firstTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        String parentTasks = firstTask.get("TaskNumber").toString();
        long parentTaskNumber = Long.parseLong(parentTasks);
        tasks.put(parentTaskNumber, firstTask);

        StringBuilder pending = new StringBuilder(parentTasks);
        for (long i = parentTaskNumber; i < parentTaskNumber + 10; i++) {
            pending.append(",").append(i);
            Map<String, Object> pendingTask = taskService.createNewTask(
                    generateTestTaskRequest(),
                    generateTestTaskCustomFieldsRequest(),
                    TaskReferencesRequest.builder().pending(pending.toString()).build());
            tasks.put((Long) pendingTask.get("TaskNumber"), pendingTask);
        }
        return tasks;
    }

    protected void create2PendingSequentialTasks() {
        Map<String, Object> firstTask = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                generateTestTaskReferenceRequest());
        String parentTaskNumber = firstTask.get("TaskNumber").toString();
        Map<String, Object> pendingTask1 = taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                TaskReferencesRequest.builder().pending(parentTaskNumber).build());
        String parentTasks = pendingTask1.get("TaskNumber").toString();
        taskService.createNewTask(
                generateTestTaskRequest(),
                generateTestTaskCustomFieldsRequest(),
                TaskReferencesRequest.builder().pending(parentTasks).build());
    }

    protected void clearAllRepositories() {
        fieldValueRepository.deleteAll();
        taskRepository.deleteAll();
        jobRepository.deleteAll();
        contactRepository.deleteAll();
        employeeRepository.deleteAll();
        groupRepository.deleteAll();
        companyRepository.deleteAll();
        professionRepository.deleteAll();
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
