package net.virtualboss.field.mapper.v1;

import net.virtualboss.common.model.entity.Field;
import net.virtualboss.field.web.dto.FieldDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FieldMapperV1 {

    Field mapToField(FieldDto fieldDto);

}
