package net.virtualboss.task.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.task.web.dto.EmployeeDto;
import net.virtualboss.task.service.EmployeeService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "employee")
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping("/employee")
    public ResponseEntity<List<EmployeeDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

}
