package net.virtualboss.mapper.v1.job;

import net.virtualboss.model.entity.Job;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class JobMapperDelegate implements JobMapperV1 {
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
    public Job addCustomFLAndGroups(Job job, CustomFieldsAndLists customFieldsAndLists, String jobGroups) {
        job.setCustomFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, EntityType.JOB));
        job.setGroups(groupService.getGroups(EntityType.JOB, jobGroups));
        return job;
    }
}
