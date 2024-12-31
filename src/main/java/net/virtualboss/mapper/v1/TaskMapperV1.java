package net.virtualboss.mapper.v1;

import net.virtualboss.model.entity.Group;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.task.TaskResponse;
import net.virtualboss.model.entity.Task;
import net.virtualboss.web.dto.task.UpsertTaskRequest;
import org.mapstruct.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {JobMapperV1.class, ContactMapperV1.class, CustomFLMapperV1.class, GroupMapperV1.class})
@DecoratedWith(TaskMapperDelegate.class)
public interface TaskMapperV1 {

    @Mapping(target = "requested", ignore = true)
    @Mapping(target = "groups", ignore = true)
    Task requestToTask(UpsertTaskRequest request, CustomFieldsAndLists customFieldsAndLists);

    default Task requestToTask(String id, UpsertTaskRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Task task = requestToTask(request, customFieldsAndLists);
        task.setId(UUID.fromString(id));
        return task;
    }

    @Mapping(source = "job.number", target = "jobNumber")
    @Mapping(source = "job", target = "jobResponse")
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "contact.person", target = "contactPerson")
    @Mapping(source = "contact.id", target = "contactId")
    @Mapping(source = "contact", target = "contactResponse")
    @Mapping(source = "requested.name", target = "requested")
    @Mapping(target = "follows", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(source = "customFieldsAndListsValues", target = "customFieldsAndLists")
    TaskResponse taskToResponse(Task task);

    default String map(Set<Group> groups) {
        return groups.stream().map(Group::getName)
                .collect(Collectors.joining (","));
    }
}
