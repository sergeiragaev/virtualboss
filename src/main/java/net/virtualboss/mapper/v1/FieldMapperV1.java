package net.virtualboss.mapper.v1;

import net.virtualboss.model.entity.Field;
import net.virtualboss.web.dto.FieldDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FieldMapperV1 {

    Field mapToField(FieldDto fieldDto);

}
