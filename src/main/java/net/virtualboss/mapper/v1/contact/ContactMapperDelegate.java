package net.virtualboss.mapper.v1.contact;

import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ContactMapperDelegate implements ContactMapperV1 {
    @Autowired
    private MainService mainService;
    @Autowired
    private GroupService groupService;

    @Override
    public Contact addCustomFLAndGroups(Contact contact, CustomFieldsAndLists customFieldsAndLists, String contactGroups) {
        contact.setCustomFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, "Contact"));
        contact.setGroups(groupService.getGroups(EntityType.CONTACT, contactGroups));
        return contact;
    }
}
