package net.virtualboss.job.mapper.v1;

import net.virtualboss.common.mapper.v1.GroupMapperV1;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.mapper.v1.ContactMapperV1;
import net.virtualboss.job.web.dto.JobResponse;
import net.virtualboss.common.model.entity.Job;
import net.virtualboss.job.web.dto.UpsertJobRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {ContactMapperV1.class, GroupMapperV1.class, JobCustomFLMapperV1.class})
@DecoratedWith(JobMapperDelegate.class)
public interface JobMapperV1 {

    @Mapping(target = "groups", ignore = true)
    Job requestToJob(UpsertJobRequest request);

    default Job requestToJob(UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Job job = requestToJob(request);
        return addCustomFLAndGroups(job, customFieldsAndLists,
                request.getGroups(), request.getOwnerId());
    }

    @Mapping(target = "owner", ignore = true)
    Job addCustomFLAndGroups(Job job, CustomFieldsAndLists customFieldsAndLists, String jobGroups, String ownerId);

    default Job requestToJob(String id, UpsertJobRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Job job = requestToJob(request, customFieldsAndLists);
        job.setId(UUID.fromString(id));
        return job;
    }

    @Mapping(source = "customFieldsAndListsValues", target = "customFieldsAndLists")
    JobResponse jobToResponse(Job job);
}
