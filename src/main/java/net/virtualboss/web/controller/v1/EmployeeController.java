package net.virtualboss.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.web.dto.EmployeeDto;
import net.virtualboss.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping("/employee")
    public ResponseEntity<List<EmployeeDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

}
