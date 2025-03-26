package net.virtualboss.job.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.AlreadyExistsException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.util.DtoFlattener;
import net.virtualboss.common.util.QueryDslUtil;
import net.virtualboss.job.mapper.v1.JobMapperV1;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import net.virtualboss.job.querydsl.JobFilterCriteria;
import net.virtualboss.job.web.dto.JobResponse;
import net.virtualboss.common.repository.JobRepository;
import net.virtualboss.job.web.dto.UpsertJobRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapperV1 jobMapper;
    private final MainService mainService;
    @PersistenceContext
    private final EntityManager entityManager;

    @Cacheable(value = "job", key = "#id")
    public Map<String, Object> findById(String id) {
        Job job = getJobById(id);
        return JobResponse.getFieldsMap(jobMapper.jobToResponse(job), null);
    }

    public List<Map<String, Object>> findAll(String fields, CommonFilter filter) {
        String mustHaveFields = "JobId,JobNumber";
        fields = fields == null ? mustHaveFields : fields + "," + mustHaveFields;
        Set<String> fieldsSet = parseFields(fields);
        List<String> fieldsList = List.copyOf(fieldsSet);

        initializeFilterDefaults(filter);
        PageRequest pageRequest = createPageRequest(filter);
        OrderSpecifier<?>[] orderSpecifiers =
                QueryDslUtil.getOrderSpecifiers(pageRequest.getSort(), Job.class, "job");

        QJob job = QJob.job;

        QFieldValue fieldValueJob = new QFieldValue("fieldValueJob");
        QField fieldJob = new QField("fieldJob");

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        List<JobResponse> jobs = queryFactory.select(
                        QueryDslUtil.buildProjection(JobResponse.class, job, fieldsList)
                )
                .from(job)
                .leftJoin(job.customFieldsAndListsValues, fieldValueJob)
                .leftJoin(fieldValueJob.field, fieldJob)
                .where(
                        buildJobFilterCriteriaQuery(filter)
                )
                .orderBy(orderSpecifiers)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .groupBy(job.id)
                .fetch();

        List<String> fieldsListForCheck = Arrays.stream(fields.split(",")).toList();
        return jobs.stream().map(response ->
                DtoFlattener.flatten(response, fieldsListForCheck)).toList();
    }

    private Predicate buildJobFilterCriteriaQuery(CommonFilter filter) {
        return JobFilterCriteria.builder()
                .findString(StringUtils.isBlank(filter.getFindString()) ? null : filter.getFindString())
                .isDeleted(filter.getIsDeleted())
                .build().toPredicate();
    }

    @Transactional
    @CacheEvict(value = "job", key = "#id")
    public void deleteJob(String id) {
        Job job = getJobById(id);
        mainService.eraseJobFromTasks(job);
        job.setIsDeleted(true);
        jobRepository.save(job);
    }

    @Transactional
    @CachePut(value = "job", key = "#id")
    public Map<String, Object> saveJob(String id, UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        checkIfJobAlreadyExist(id, request);
        Job job = jobMapper.requestToJob(id, request, customFieldsAndLists);
        Job jobFromDb = getJobById(id);
        job.getCustomFieldsAndListsValues().addAll(jobFromDb.getCustomFieldsAndListsValues());
        BeanUtils.copyNonNullProperties(job, jobFromDb);
        return JobResponse.getFieldsMap(jobMapper.jobToResponse(jobRepository.save(jobFromDb)), null);
    }

    @Transactional
    public Map<String, Object> createJob(UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        checkIfJobAlreadyExist(null, request);
        Job job = jobMapper.requestToJob(request, customFieldsAndLists);
        return JobResponse.getFieldsMap(jobMapper.jobToResponse(jobRepository.save(job)), null);
    }

    public Job getJobById(String id) {
        return jobRepository.findById(UUID.fromString(id))
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                MessageFormat.format("Job with Id: {0} not found!", id)
                        ));
    }

    private void checkIfJobAlreadyExist(String id, UpsertJobRequest request) {
        UUID uuid = id == null ? null : UUID.fromString(id);
        String jobNumber = request.getNumber();
        Optional<Job> optionalJob = jobRepository.findByNumberIgnoreCaseAndIsDeleted(jobNumber, false);
        if (optionalJob.isPresent()  && !optionalJob.get().getId().equals(uuid))
            throw new AlreadyExistsException(MessageFormat.format("Job with number <b>{0}</b> already exists!", jobNumber));
    }

    private void initializeFilterDefaults(CommonFilter filter) {
        if (filter.getSize() == null) filter.setSize(Integer.MAX_VALUE);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("number asc");
    }

    private PageRequest createPageRequest(CommonFilter filter) {
        return PageRequest.of(
                filter.getPage() - 1,
                filter.getSize(),
                Sort.by(parseSortOrders(filter.getSort()))
        );
    }

    private List<Sort.Order> parseSortOrders(String sortString) {
        return Arrays.stream(sortString.split(","))
                .map(this::createSortOrder)
                .toList();
    }

    private Sort.Order createSortOrder(String sort) {
        String[] parts = sort.trim().split(" ");
        Sort.Direction direction = Sort.Direction.valueOf(parts[1].toUpperCase());
        return new Sort.Order(direction, parts[0]);
    }

    private Set<String> parseFields(String fields) {
        return Arrays.stream(fields.split(","))
                .map(string -> {
                    if (string.contains("JobCustom")) return "JobCustomFieldsAndLists." + string;
                    return string;
                })
                .collect(Collectors.toSet());
    }
}
