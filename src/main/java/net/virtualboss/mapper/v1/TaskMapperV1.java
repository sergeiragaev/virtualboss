package net.virtualboss.mapper.v1;

import net.virtualboss.model.dto.TaskDto;
import net.virtualboss.model.entity.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TaskMapperV1 {

//    Task mapToTask(TaskDto taskDto);

    @Named("mapWithoutFollows")
    @Mapping(source = "job.number", target = "jobNumber")
    @Mapping(source = "contact.person", target = "contactPerson")
    @Mapping(source = "employee.name", target = "employee")
    @Mapping(target = "follows", ignore = true)
    TaskDto mapToDto(Task task);



    @IterableMapping(qualifiedByName="mapWithoutFollows")
    List<TaskDto> map(List<Task> tasks);
}
