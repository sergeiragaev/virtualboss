package net.virtualboss.mapper.v1.contact;

import net.virtualboss.model.entity.FieldValue;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
@DecoratedWith(ContactCustomFLMapperDelegate.class)
public interface ContactCustomFLMapperV1 {

    record FieldsWrapper(Set<FieldValue> values) {}

    default CustomFieldsAndLists map(Set<FieldValue> values) {
        return map(new FieldsWrapper(values));
    }

    CustomFieldsAndLists map(FieldsWrapper wrapper);
}
