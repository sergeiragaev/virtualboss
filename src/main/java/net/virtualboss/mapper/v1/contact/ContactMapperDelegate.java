package net.virtualboss.mapper.v1.contact;

import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class ContactMapperDelegate implements ContactMapperV1 {
    private MainService mainService;
    private GroupService groupService;

    @Autowired
    public void setMainService(MainService mainService) {
        this.mainService = mainService;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public Contact addCustomFLAndGroups(Contact contact, CustomFieldsAndLists customFieldsAndLists, String contactGroups) {
        contact.setCustomFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, EntityType.CONTACT));
        contact.setGroups(groupService.getGroups(EntityType.CONTACT, contactGroups));
        return contact;
    }
}
