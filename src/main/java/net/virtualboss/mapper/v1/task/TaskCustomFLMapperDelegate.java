package net.virtualboss.mapper.v1.task;

import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TaskCustomFLMapperDelegate implements TaskCustomFLMapperV1 {
    @Autowired
    private MainService mainService;

    @Override
    public CustomFieldsAndLists map(FieldsWrapper wrapper) {
        return mainService.setCustomFieldsAndLists(wrapper.values(), EntityType.TASK);
    }
}
