package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.EntityNotFoundException;
import net.virtualboss.mapper.v1.EmployeeMapperV1;
import net.virtualboss.web.dto.EmployeeDto;
import net.virtualboss.model.entity.Employee;
import net.virtualboss.repository.EmployeeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapperV1 mapper;

    @Cacheable(value = "employee", key = "#id")
    public EmployeeDto[] findById(String id) {
        Employee employee = employeeRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Employee with id: {0} not found", id)));
        return new EmployeeDto[]{mapper.mapToDto(employee)};
    }

    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll().stream().map(mapper::mapToDto).toList();
    }
}
