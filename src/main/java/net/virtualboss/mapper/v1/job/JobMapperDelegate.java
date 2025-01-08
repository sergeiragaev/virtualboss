package net.virtualboss.mapper.v1.job;

import net.virtualboss.model.entity.Job;
import net.virtualboss.model.enums.EntityType;
import net.virtualboss.service.GroupService;
import net.virtualboss.service.MainService;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JobMapperDelegate implements JobMapperV1 {
    @Autowired
    private MainService mainService;
    @Autowired
    private GroupService groupService;

    @Override
    public Job addCustomFLAndGroups(Job job, CustomFieldsAndLists customFieldsAndLists, String jobGroups) {
        job.setCustomFieldsAndListsValues(mainService.createCustomList(customFieldsAndLists, "Job"));
        job.setGroups(groupService.getGroups(EntityType.JOB, jobGroups));
        return job;
    }
}
