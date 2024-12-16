package net.virtualboss.mapper.v1;

import net.virtualboss.web.dto.ContactDto;
import net.virtualboss.model.entity.Contact;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ContactMapperV1 {

    Contact map(ContactDto contactDto);

    @Mapping(target = "email")
    ContactDto mapToDto(Contact contact);

    List<ContactDto> map(List<Contact> contacts);
}
