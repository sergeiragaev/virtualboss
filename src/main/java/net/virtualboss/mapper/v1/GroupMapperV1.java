package net.virtualboss.mapper.v1;

import net.virtualboss.model.entity.Group;
import net.virtualboss.web.dto.GroupDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface GroupMapperV1 {

    Group mapToEntity(GroupDto groupDto);

    GroupDto mapToDto(Group group);

    List<GroupDto> map(List<Group> groups);

    default String mapToGroups(Set<Group> groups) {
        return groups.stream().map(Group::getName)
                .collect(Collectors.joining(","));
    }
}
