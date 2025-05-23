package net.virtualboss.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.task.mapper.v1.EmployeeMapperV1;
import net.virtualboss.task.web.dto.EmployeeDto;
import net.virtualboss.common.model.entity.Employee;
import net.virtualboss.common.repository.EmployeeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
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

    @Cacheable(value = "employee")
    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll(
                Sort.by(Sort.Direction.ASC, "name")
        ).stream().map(mapper::mapToDto).toList();
    }

    public Employee findByName(String name) {
        if (name == null || name.isBlank()) return null;
        return employeeRepository.findByName(name).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Employee with name: {0} not found!", name)));
    }
}
