package net.virtualboss.mapper.v1;

import net.virtualboss.model.entity.Group;
import net.virtualboss.web.dto.GroupDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface GroupMapperV1 {

    Group mapToEntity(GroupDto groupDto);

    GroupDto mapToDto(Group group);

    List<GroupDto> map(List<Group> groups);
}
