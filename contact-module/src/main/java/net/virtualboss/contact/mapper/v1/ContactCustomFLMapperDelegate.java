package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.CustomFieldService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class ContactCustomFLMapperDelegate implements ContactCustomFLMapperV1 {
    private CustomFieldService customFieldService;

    @Autowired
    public void setCustomFieldService(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    @Override
    public CustomFieldsAndLists map(FieldsWrapper wrapper) {
        return customFieldService.populateCustomFields(wrapper.values(), EntityType.CONTACT);
    }
}
