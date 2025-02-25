package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class ContactCustomFLMapperDelegate implements ContactCustomFLMapperV1 {
    private MainService mainService;

    @Autowired
    public void setMainService(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    public CustomFieldsAndLists map(FieldsWrapper wrapper) {
        return mainService.setCustomFieldsAndLists(wrapper.values(), EntityType.CONTACT);
    }
}
