package net.virtualboss.task.mapper.v1;

import net.virtualboss.contact.mapper.v1.ContactMapperV1;
import net.virtualboss.common.mapper.v1.GroupMapperV1;
import net.virtualboss.job.mapper.v1.JobMapperV1;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.task.web.dto.TaskReferencesRequest;
import net.virtualboss.task.web.dto.TaskResponse;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.task.web.dto.UpsertTaskRequest;
import org.mapstruct.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {JobMapperV1.class, ContactMapperV1.class, GroupMapperV1.class, TaskCustomFLMapperV1.class})
@DecoratedWith(TaskMapperDelegate.class)
public interface TaskMapperV1 {

    default Task requestToTask(UpsertTaskRequest request,
                               CustomFieldsAndLists customFieldsAndLists,
                               TaskReferencesRequest referenceRequest) {
        Task task = requestToTask(request);
        return setCFLAndReferencesToTask(task,
                customFieldsAndLists, referenceRequest);
    }

    @Mapping(target = "requested", ignore = true)
    @Mapping(target = "follows", ignore = true)
    @Mapping(target = "groups", ignore = true)
    Task setCFLAndReferencesToTask(Task task, CustomFieldsAndLists customFieldsAndLists, TaskReferencesRequest request);

    Task requestToTask(UpsertTaskRequest request);

    default Task requestToTask(String id,
                               UpsertTaskRequest request,
                               CustomFieldsAndLists customFieldsAndLists,
                               TaskReferencesRequest referenceRequest) {
        Task task = requestToTask(request, customFieldsAndLists, referenceRequest);
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
    @Mapping(source = "customFieldsAndListsValues", target = "customFieldsAndLists")
    TaskResponse taskToResponse(Task task);

    default String map(Set<Task> tasks) {
        return tasks.stream().sorted().map(task -> task.getNumber().toString())
                .collect(Collectors.joining(","));
    }

}
