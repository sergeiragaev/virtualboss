package net.virtualboss.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.AlreadyExistsException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.service.MainService;
import net.virtualboss.job.mapper.v1.JobMapperV1;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.job.repository.criteria.JobFilterCriteria;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import net.virtualboss.job.web.dto.JobResponse;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.common.repository.JobRepository;
import net.virtualboss.job.web.dto.UpsertJobRequest;
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

    @Cacheable(value = "job", key = "#id")
    public Map<String, Object> findById(String id) {
        Job job = getJobById(id);
        return JobResponse.getFieldsMap(jobMapper.jobToResponse(job), null);
    }

    public List<Map<String, Object>> findAll(String fields, CommonFilter commonFilter) {
        if (fields == null) fields = "JobId,JobNumber";
        Set<String> fieldList = Arrays.stream(fields.split(",")).collect(Collectors.toSet());

        if (commonFilter.getSize() == null) commonFilter.setSize(Integer.MAX_VALUE);
        if (commonFilter.getPage() == null) commonFilter.setPage(1);
        if (commonFilter.getSort() == null) commonFilter.setSort("number asc");

        String[] sorts = commonFilter.getSort().split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] order = sort.split(" ");
            orders.add(new Sort.Order(Sort.Direction.valueOf(order[1].toUpperCase()), order[0]));
        }

        return jobRepository.findAll(
                        JobFilterCriteria.builder()
                                .isDeleted(commonFilter.getIsDeleted())
                                .findString(commonFilter.getFindString() == null || commonFilter.getFindString().isBlank() ? null : commonFilter.getFindString())
                                .build().getSpecification(),
                        PageRequest.of(commonFilter.getPage() - 1, commonFilter.getSize(),
                                Sort.by(orders)
                        ))
                .map(jobMapper::jobToResponse).getContent().stream()
                .map(jobResponse -> JobResponse.getFieldsMap(jobResponse, fieldList))
                .toList();
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
}
