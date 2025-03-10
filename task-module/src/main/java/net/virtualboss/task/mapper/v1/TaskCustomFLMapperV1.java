package net.virtualboss.task.mapper.v1;

import net.virtualboss.common.model.entity.FieldValue;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
@DecoratedWith(TaskCustomFLMapperDelegate.class)
public interface TaskCustomFLMapperV1 {

    record FieldsWrapper(Set<FieldValue> values) {}

    default CustomFieldsAndLists map(Set<FieldValue> values) {
        return map(new FieldsWrapper(values));
    }

    CustomFieldsAndLists map(FieldsWrapper wrapper);
}
