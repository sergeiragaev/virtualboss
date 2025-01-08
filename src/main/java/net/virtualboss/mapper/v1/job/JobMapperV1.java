package net.virtualboss.mapper.v1.job;

import net.virtualboss.mapper.v1.GroupMapperV1;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.job.JobResponse;
import net.virtualboss.model.entity.Job;
import net.virtualboss.web.dto.job.UpsertJobRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {GroupMapperV1.class, JobCustomFLMapperV1.class})
@DecoratedWith(JobMapperDelegate.class)
public interface JobMapperV1 {

    @Mapping(target = "groups", ignore = true)
    Job requestToJob(UpsertJobRequest request);

    default Job requestToJob(UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Job job = requestToJob(request);
        return addCustomFLAndGroups(job, customFieldsAndLists,
                request.getGroups());
    }

    Job addCustomFLAndGroups(Job job, CustomFieldsAndLists customFieldsAndLists, String jobGroups);

    default Job requestToJob(String id, UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Job job = requestToJob(request, customFieldsAndLists);
        job.setId(UUID.fromString(id));
        return job;
    }

    @Mapping(source = "customFieldsAndListsValues", target = "customFieldsAndLists")
    JobResponse jobToResponse(Job job);
}
