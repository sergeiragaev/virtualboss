package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.model.entity.Employee;
import net.virtualboss.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeRepository repository;

    @GetMapping("/RequestedByData")
    public ResponseEntity<List<Employee>> requestedByData() {
        return ResponseEntity.ok(repository.findAll());
    }

}
