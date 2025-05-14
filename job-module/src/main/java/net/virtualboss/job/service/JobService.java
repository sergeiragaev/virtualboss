package net.virtualboss.job.service;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.AlreadyExistsException;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.repository.FieldRepository;
import net.virtualboss.common.service.GenericService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.job.mapper.v1.JobMapperV1;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import net.virtualboss.job.mapper.v1.JobResponseMapper;
import net.virtualboss.job.querydsl.JobFilterCriteria;
import net.virtualboss.job.web.dto.JobResponse;
import net.virtualboss.common.repository.JobRepository;
import net.virtualboss.job.web.dto.UpsertJobRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class JobService extends GenericService<Job, UUID, JobResponse, QJob> {
    private final JobRepository repository;
    private final JobMapperV1 mapper;
    private final JobResponseMapper jobResponseMapper;
    private final Map<String, String> customMappings = Map.of(
            "JobCustom", "customFields.",
            "ContactCustom", "owner.customFields."
    );

    public JobService(EntityManager entityManager,
                      MainService mainService,
                      JobRepository jobRepository,
                      JobMapperV1 jobMapper,
                      JobResponseMapper jobResponseMapper,
                      FieldRepository fieldRepository) {
        super(entityManager, mainService, UUID::fromString, jobRepository, fieldRepository);
        this.repository = jobRepository;
        this.mapper = jobMapper;
        this.jobResponseMapper = jobResponseMapper;
    }

    @Override
    protected Map<String, String> getCustomMappings() {
        return customMappings;
    }

    @Override
    protected Map<String, String> getNestedMappings() {
        return Map.of(
                "Contact", "owner."
        );
    }

    @Override
    protected QJob getQEntity() {
        return QJob.job;
    }

    @Override
    protected Predicate buildFilterPredicate(CommonFilter filter) {
        return JobFilterCriteria.builder()
                .findString(StringUtils.defaultIfBlank(filter.getFindString(), null))
                .isDeleted(filter.getIsDeleted())
                .build()
                .toPredicate();
    }

    @Override
    protected String getCustomFieldPrefix() {
        return "JobCustom";
    }

    @Override
    protected String getCustomFieldsAndListsPrefix() {
        return "JobCustomFieldsAndLists";
    }

    @Override
    protected String getDefaultSort() {
        return "number:asc";
    }

    @Override
    protected String getMustHaveFields() {
        return "JobId,JobNumber";
    }

    @Override
    protected Class<Job> getEntityClass() {
        return Job.class;
    }

    @Override
    protected Class<JobResponse> getResponseClass() {
        return JobResponse.class;
    }

    @Override
    protected List<JoinExpression> getJoins() {
        QJob job = QJob.job;
        QFieldValue fieldValueJob = new QFieldValue("fieldValueJob");
        QField fieldJob = new QField("fieldJob");
        QCompany company = QCompany.company;

        return List.of(
                new CollectionJoin<>(job.customFieldsAndListsValues, fieldValueJob, JoinType.LEFTJOIN),
                new EntityJoin<>(fieldValueJob.field, fieldJob, JoinType.LEFTJOIN),
                new EntityJoin<>(job.owner, QContact.contact, JoinType.LEFTJOIN),
                new EntityJoin<>(QContact.contact.company, company, JoinType.LEFTJOIN)
        );
    }

    @Override
    protected List<GroupByExpression> getGroupBy() {
        return List.of(
                new GroupByExpression(QJob.job.id),
                new GroupByExpression(QCompany.company.id),
                new GroupByExpression(QContact.contact.id)
        );
    }

    @Cacheable(value = "job", key = "#id")
    public Map<String, Object> getById(String id) {
        Job job = findById(id);
        return jobResponseMapper.map(mapper.jobToResponse(job), null);
    }

    @Transactional
    @CacheEvict(value = "job", key = "#id")
    public void deleteJob(String id) {
        Job job = findById(id);
        mainService.eraseJobFromTasks(job);
        job.setIsDeleted(true);
        repository.save(job);
    }

    @Transactional
    @CachePut(value = "job", key = "#id")
    public Map<String, Object> saveJob(String id, UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        checkIfJobAlreadyExist(id, request);
        Job job = mapper.requestToJob(id, request, customFieldsAndLists);
        Job jobFromDb = findById(id);
        job.getCustomFieldsAndListsValues().addAll(jobFromDb.getCustomFieldsAndListsValues());
        BeanUtils.copyNonNullProperties(job, jobFromDb);
        return jobResponseMapper.map(mapper.jobToResponse(repository.save(jobFromDb)), null);
    }

    @Transactional
    public Map<String, Object> createJob(UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        checkIfJobAlreadyExist(null, request);
        Job job = mapper.requestToJob(request, customFieldsAndLists);
        return jobResponseMapper.map(mapper.jobToResponse(repository.save(job)), null);
    }

    private void checkIfJobAlreadyExist(String id, UpsertJobRequest request) {
        UUID uuid = id == null ? null : UUID.fromString(id);
        String jobNumber = request.getNumber();
        Optional<Job> optionalJob = repository.findByNumberIgnoreCaseAndIsDeleted(jobNumber, false);
        if (optionalJob.isPresent() && !optionalJob.get().getId().equals(uuid))
            throw new AlreadyExistsException(MessageFormat.format("Job with number <b>{0}</b> already exists!", jobNumber));
    }

    @Override
    protected Set<String> parseFields(String fields) {
        return Arrays.stream(fields.split(","))
                .map(field -> {
                    if (field.contains(getCustomFieldPrefix())) return getCustomFieldsAndListsPrefix() + "." + field;
                    if (field.contains("ContactCustom")) return "owner.ContactCustomFieldsAndLists." + field;
                    if (field.equals("ContactCompany")) return "owner.company." + field;
                    if (field.startsWith("Contact")) return "owner." + field;
                    return field;
                })
                .collect(Collectors.toSet());
    }

}
