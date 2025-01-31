package net.virtualboss.mapper.v1.job;

import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class JobCustomFLMapperDelegate implements JobCustomFLMapperV1 {
    private MainService mainService;

    @Autowired
    public void setMainService(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    public CustomFieldsAndLists map(FieldsWrapper wrapper) {
        return mainService.setCustomFieldsAndLists(wrapper.values(), EntityType.JOB);
    }
}
