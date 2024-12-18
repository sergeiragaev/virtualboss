package net.virtualboss.mapper.v1;

import net.virtualboss.web.dto.contact.ContactResponse;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.web.dto.contact.UpsertContactRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ContactMapperV1 {

    Contact requestToContact(UpsertContactRequest request);

    default Contact requestToContact(String id, UpsertContactRequest request) {
        Contact contact = requestToContact(request);
        contact.setId(UUID.fromString(id));
        return contact;
    }

    @Mapping(target = "email")
    ContactResponse contactToResponse(Contact contact);

    List<ContactResponse> map(List<Contact> contacts);
}
