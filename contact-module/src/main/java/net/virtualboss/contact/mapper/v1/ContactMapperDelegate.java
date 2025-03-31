package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.CustomFieldService;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class ContactMapperDelegate implements ContactMapperV1 {
    private CustomFieldService customFieldService;
    private GroupService groupService;

    @Autowired
    public void setCustomFieldService(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public Contact addCustomFLAndGroups(Contact contact, CustomFieldsAndLists customFieldsAndLists, String contactGroups) {
        contact.setCustomFieldsAndListsValues(customFieldService.createCustomFieldValues(customFieldsAndLists, EntityType.CONTACT));
        contact.setGroups(groupService.getGroups(EntityType.CONTACT, contactGroups));
        return contact;
    }
}
