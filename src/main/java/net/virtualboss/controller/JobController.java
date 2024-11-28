package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.model.dto.JobDto;
import net.virtualboss.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class JobController {
    private final JobService service;

    @GetMapping("/jobfeed")
    public ResponseEntity<List<JobDto>> jobFeed() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/jobdata")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<JobDto[]> jobData(@RequestParam String JobId) {
        return ResponseEntity.ok(service.findById(JobId));
    }

}
