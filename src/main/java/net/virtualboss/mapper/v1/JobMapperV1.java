package net.virtualboss.mapper.v1;

import net.virtualboss.web.dto.JobDto;
import net.virtualboss.model.entity.Job;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JobMapperV1 {

    Job mapToJob(JobDto jobDto);

    JobDto mapToDto(Job job);

    List<JobDto> map(List<Job> jobs);
}
