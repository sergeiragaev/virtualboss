package net.virtualboss.job.mapper.v1;

import net.virtualboss.common.model.entity.Job;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.GroupService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
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
