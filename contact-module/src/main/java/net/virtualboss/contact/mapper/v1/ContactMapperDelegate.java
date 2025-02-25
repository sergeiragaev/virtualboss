package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
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
