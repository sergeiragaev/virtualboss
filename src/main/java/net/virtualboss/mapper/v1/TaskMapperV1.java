package net.virtualboss.mapper.v1;

import net.virtualboss.web.dto.TaskDto;
import net.virtualboss.model.entity.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
@DecoratedWith(TaskMapperDelegate.class)
public interface TaskMapperV1 {

    @Named("mapWithoutFollows")
    @Mapping(target = "follows", ignore = true)
    @Mapping(target = "requested", ignore = true)
    Task mapToEntity(TaskDto taskDto);

    @Named("mapWithoutFollows")
    @Mapping(source = "job.number", target = "jobNumber")
    @Mapping(source = "contact.person", target = "contactPerson")
    @Mapping(source = "contact.id", target = "contactId")
    @Mapping(source = "requested.name", target = "requested")
    @Mapping(target = "follows", ignore = true)
    TaskDto mapToDto(Task task);

    @IterableMapping(qualifiedByName="mapWithoutFollows")
    List<TaskDto> mapToDtoList(List<Task> tasks);
}
