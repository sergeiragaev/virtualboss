package net.virtualboss.mapper.v1;

import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CustomFLMapperDelegate implements CustomFLMapperV1 {
    @Autowired
    private MainService mainService;

    @Override
    public CustomFieldsAndLists map(FieldsWrapper wrapper) {
        return mainService.getCustomFieldsAndLists(wrapper.values(), "Task");
    }
}
