package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.mapper.v1.JobMapperV1;
import net.virtualboss.model.dto.JobDto;
import net.virtualboss.model.entity.Job;
import net.virtualboss.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapperV1 jobMapper;

    public JobDto[] findById(String id) {
        Job job = jobRepository.findById(UUID.fromString(id)).orElse(null);
        return new JobDto[]{jobMapper.mapToDto(job)};
    }

    public List<JobDto> findAll() {
        return jobMapper.map(jobRepository.findAll());
    }
}
