package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.AlreadyExistsException;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.mapper.v1.JobMapperV1;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.repository.criteria.JobFilterCriteria;
import net.virtualboss.web.dto.filter.CommonFilter;
import net.virtualboss.web.dto.job.JobResponse;
import net.virtualboss.model.entity.Job;
import net.virtualboss.repository.JobRepository;
import net.virtualboss.web.dto.job.UpsertJobRequest;
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
    public JobResponse findById(String id) {
        Job job = getJobById(id);
        return jobMapper.jobToResponse(job);
    }

    public List<Map<String, Object>> findAll(String fields, CommonFilter commonFilter) {

        Set<String> fieldList = Arrays.stream(fields.split(",")).collect(Collectors.toSet());

        if (commonFilter.getSize() == null) commonFilter.setSize(Integer.MAX_VALUE);
        if (commonFilter.getPage() == null) commonFilter.setPage(1);
        if (commonFilter.getSort() == null) commonFilter.setSort("number,asc");
        String[] sorts = commonFilter.getSort().split(",");

        return jobRepository.findAll(
                        JobFilterCriteria.builder()
                                .findString(commonFilter.getFindString() == null || commonFilter.getFindString().isBlank() ? null : commonFilter.getFindString())
                                .build().getSpecification(),
                        PageRequest.of(commonFilter.getPage() - 1, commonFilter.getSize(),
                                Sort.by(
                                        Sort.Direction.valueOf(sorts[1].toUpperCase()), sorts[0]
                                )
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
        jobRepository.delete(job);
    }

    @Transactional
    @CachePut(value = "job", key = "#id")
    public JobResponse saveJob(String id, UpsertJobRequest request) {
        checkIfJobAlreadyExist(id, request);
        Job job = jobMapper.requestToJob(id, request);
        Job jobFromDb = getJobById(id);
        BeanUtils.copyNonNullProperties(job, jobFromDb);
        return jobMapper.jobToResponse(jobRepository.save(jobFromDb));
    }

    @Transactional
    public JobResponse createJob(UpsertJobRequest request) {
        checkIfJobAlreadyExist(null, request);
        Job job = jobMapper.requestToJob(request);
        return jobMapper.jobToResponse(jobRepository.save(job));
    }

    private Job getJobById(String id) {
        return jobRepository.findById(UUID.fromString(id))
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                MessageFormat.format("Job with Id: {0} not found!", id)
                        ));
    }

    private void checkIfJobAlreadyExist(String id, UpsertJobRequest request) {
        UUID uuid = id == null ? null : UUID.fromString(id);
        String jobNumber = request.getNumber();
        Optional<Job> optionalJob = jobRepository.findByNumberIgnoreCase(jobNumber);
        if (optionalJob.isPresent()  && !optionalJob.get().getId().equals(uuid))
            throw new AlreadyExistsException(MessageFormat.format("Job with number <b>{0}</b> already exists!", jobNumber));
    }
}
