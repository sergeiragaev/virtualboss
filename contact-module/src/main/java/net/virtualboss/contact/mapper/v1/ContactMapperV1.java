package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.mapper.v1.GroupMapperV1;
import net.virtualboss.common.model.entity.Phone;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import org.mapstruct.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {GroupMapperV1.class, ContactCustomFLMapperV1.class})
@DecoratedWith(ContactMapperDelegate.class)
public interface ContactMapperV1 {

    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "phones", ignore = true)
    Contact requestToContact(UpsertContactRequest request);

    default Contact requestToContact(UpsertContactRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Contact contact = requestToContact(request);
        return addCustomFLAndGroups(contact, customFieldsAndLists,
                request.getGroups());
    }

    default Contact requestToContact(String id, UpsertContactRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Contact contact = requestToContact(request, customFieldsAndLists);
        contact.setId(UUID.fromString(id));
        return contact;
    }

    Contact addCustomFLAndGroups(Contact contact, CustomFieldsAndLists customFieldsAndLists, String jobGroups);

    @Mapping(source = "customFieldsAndListsValues", target = "customFieldsAndLists")
    ContactResponse contactToResponse(Contact contact);

    default String mapPhones(Set<Phone> phones) {
        if (phones == null || phones.isEmpty()) {
            return "";
        }
        return phones.stream()
                .map(phone ->
                    phone.getPhoneType().getDescription() + ": " + phone.getPhoneNumber()
                ).collect(Collectors.joining(","));
    }
}
