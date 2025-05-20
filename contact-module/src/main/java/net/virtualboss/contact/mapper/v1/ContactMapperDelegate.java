package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.model.entity.Address;
import net.virtualboss.common.model.entity.Communication;
import net.virtualboss.common.model.entity.CommunicationType;
import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.enums.ChannelType;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.repository.CommunicationTypeRepository;
import net.virtualboss.common.service.CustomFieldService;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.ContactReferencesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public abstract class ContactMapperDelegate implements ContactMapperV1 {
    private CustomFieldService customFieldService;
    private GroupService groupService;
    private MainService mainService;
    private CommunicationTypeRepository communicationTypeRepository;

    @Autowired
    public void setCustomFieldService(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Autowired
    public void setMainService(MainService mainService) {
        this.mainService = mainService;
    }

    @Autowired
    public void setCommunicationTypeRepository(CommunicationTypeRepository communicationTypeRepository) {
        this.communicationTypeRepository = communicationTypeRepository;
    }

    @Override
    public Contact addCustomFLAndGroups(Contact contact, CustomFieldsAndLists customFieldsAndLists, ContactReferencesRequest request) {
        contact.setCustomFieldsAndListsValues(customFieldService.createCustomFieldValues(customFieldsAndLists, EntityType.CONTACT));
        contact.setGroups(groupService.getGroups(EntityType.CONTACT, request.getGroups()));
        contact.setCompany(mainService.getCompany(request.getCompany()));
        contact.setProfession(mainService.getProfession(request.getProfession()));
        contact.setPhones(mapPhones(request.getPhones(), contact));
        contact.setAddresses(mapAddresses(request.getAddresses(), contact));
        return contact;
    }

    public Set<Communication> mapPhones(String phones, Contact contact) {
        if (phones == null || phones.isBlank()) return Set.of();

        return Arrays.stream(phones.split(","))
                .map(String::trim)
                .filter(s -> s.contains(":"))
                .map(entry -> {
                    String[] parts = entry.split(":", 2);
                    String phoneCaption = parts[0].trim();
                    String title = parts[1].trim();

                    CommunicationType type = resolveOrCreateType(phoneCaption, ChannelType.PHONE);

                    return Communication.builder()
                            .contact(contact)
                            .type(type)
                            .title(title)
                            .build();
                })
                .collect(Collectors.toSet());
    }

    public Set<Address> mapAddresses(String addresses, Contact contact) {
        if (addresses == null || addresses.isBlank()) return Set.of();

        return Arrays.stream(addresses.split(";"))
                .map(String::trim)
                .filter(s -> s.contains(":"))
                .map(entry -> {
                    String[] parts = entry.split(":", 2);
                    String addressCaption = parts[0].trim();
                    String body = parts[1].trim();
                    String[] fields = body.split(",");

                    CommunicationType type = resolveOrCreateType(addressCaption, ChannelType.ADDRESS);

                    return Address.builder()
                            .contact(contact)
                            .type(type)
                            .address1(fields.length > 0 ? fields[0].trim() : "")
                            .address2(fields.length > 1 ? fields[1].trim() : "")
                            .city(fields.length > 2 ? fields[2].trim() : "")
                            .state(fields.length > 3 ? fields[3].trim() : "")
                            .postal(fields.length > 4 ? fields[4].trim() : "")
                            .build();
                })
                .collect(Collectors.toSet());
    }

    private CommunicationType resolveOrCreateType(String caption, ChannelType channel) {
        return communicationTypeRepository
                .findByCaptionIgnoreCaseAndChannel(caption, channel)
                .orElseGet(() -> {
                    CommunicationType newType = CommunicationType.builder()
                            .caption(caption)
                            .channel(channel)
                            .build();
                    return communicationTypeRepository.saveAndFlush(newType);
                });
    }
}
