package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.model.entity.Contact;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.CustomFieldService;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class ContactMapperDelegate implements ContactMapperV1 {
    private CustomFieldService customFieldService;
    private GroupService groupService;
    private MainService mainService;

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

    @Override
    public Contact addCustomFLAndGroups(Contact contact, CustomFieldsAndLists customFieldsAndLists, String contactGroups, String company, String profession) {
        contact.setCustomFieldsAndListsValues(customFieldService.createCustomFieldValues(customFieldsAndLists, EntityType.CONTACT));
        contact.setGroups(groupService.getGroups(EntityType.CONTACT, contactGroups));
        contact.setCompany(mainService.getCompany(company));
        contact.setProfession(mainService.getProfession(profession));
        return contact;
    }
}
