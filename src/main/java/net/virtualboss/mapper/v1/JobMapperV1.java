package net.virtualboss.mapper.v1;

import net.virtualboss.web.dto.job.JobResponse;
import net.virtualboss.model.entity.Job;
import net.virtualboss.web.dto.job.UpsertJobRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JobMapperV1 {

    Job requestToJob(UpsertJobRequest request);

    default Job requestToJob(String id, UpsertJobRequest request) {
        Job job = requestToJob(request);
        job.setId(UUID.fromString(id));
        return job;
    }

    JobResponse jobToResponse(Job job);
}
