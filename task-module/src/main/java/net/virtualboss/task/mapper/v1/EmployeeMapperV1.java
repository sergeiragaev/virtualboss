package net.virtualboss.task.mapper.v1;

import net.virtualboss.task.web.dto.EmployeeDto;
import net.virtualboss.common.model.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface EmployeeMapperV1 {

    Employee mapToEntity(EmployeeDto employeeDto);

    EmployeeDto mapToDto(Employee employee);

    List<EmployeeDto> map(List<Employee> employees);
}
