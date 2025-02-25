package net.virtualboss.job.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import net.virtualboss.job.service.JobService;
import net.virtualboss.job.web.dto.UpsertJobRequest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "job")
public class JobController {
    private final JobService service;

    @GetMapping("/job")
    public ResponseEntity<List<Map<String, Object>>> getJobs(
            @RequestParam(required = false) String fields,
            CommonFilter commonFilter) {
        return ResponseEntity.ok(service.findAll(fields, commonFilter));
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<Map<String, Object>> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/job/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteJob(@PathVariable String id) {
        service.deleteJob(id);
    }

    @PutMapping("/job/{id}")
    public ResponseEntity<Map<String, Object>> saveJob(
            @PathVariable String id,
            UpsertJobRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        return ResponseEntity.ok(service.saveJob(id, request, customFieldsAndLists));
    }

    @PostMapping("/job")
    public ResponseEntity<Map<String, Object>> createJob(
            UpsertJobRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        return new ResponseEntity<>(service.createJob(request, customFieldsAndLists), HttpStatus.CREATED);
    }

}
