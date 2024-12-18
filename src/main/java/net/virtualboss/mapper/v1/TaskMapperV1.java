package net.virtualboss.mapper.v1;

import net.virtualboss.web.dto.task.TaskResponse;
import net.virtualboss.model.entity.Task;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {JobMapperV1.class, ContactMapperV1.class})
@DecoratedWith(TaskMapperDelegate.class)
public interface TaskMapperV1 {

    @Mapping(target = "requested", ignore = true)
    Task requestToTask(UpsertTaskRequest request);

    default Task requestToTask(String id, UpsertTaskRequest request) {
        Task task = requestToTask(request);
        task.setId(UUID.fromString(id));
        return task;
    }

//    @Named("mapWithoutFollows")
    @Mapping(source = "job.number", target = "jobNumber")
    @Mapping(source = "job", target = "jobResponse")
    @Mapping(source = "contact.person", target = "contactPerson")
    @Mapping(source = "contact.id", target = "contactId")
    @Mapping(source = "contact", target = "contactResponse")
    @Mapping(source = "requested.name", target = "requested")
    @Mapping(target = "follows", ignore = true)
    TaskResponse taskToResponse(Task task);

}
