package net.virtualboss.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.web.dto.filter.Filter;
import net.virtualboss.web.dto.job.JobResponse;
import net.virtualboss.service.JobService;
import net.virtualboss.web.dto.job.UpsertJobRequest;
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
            Filter filter) {
        return ResponseEntity.ok(service.findAll(fields, filter));
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/job/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteJob(@PathVariable String id) {
        service.deleteJob(id);
    }

    @PutMapping("/job/{id}")
    public ResponseEntity<JobResponse> saveJob(
            @PathVariable String id,
            UpsertJobRequest request) {
        return ResponseEntity.ok(service.saveJob(id, request));
    }

    @PostMapping("/job")
    public ResponseEntity<JobResponse> createJob(UpsertJobRequest request) {
        return ResponseEntity.ok(service.createJob(request));
    }

}
