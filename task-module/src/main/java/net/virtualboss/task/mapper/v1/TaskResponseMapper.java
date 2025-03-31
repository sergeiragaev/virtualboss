package net.virtualboss.task.mapper.v1;

import lombok.AllArgsConstructor;
import net.virtualboss.common.mapper.v1.AbstractResponseMapper;
import net.virtualboss.common.mapper.v1.CustomFieldsMapper;
import net.virtualboss.contact.mapper.v1.ContactResponseMapper;
import net.virtualboss.job.mapper.v1.JobResponseMapper;
import net.virtualboss.task.web.dto.TaskResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public class TaskResponseMapper extends AbstractResponseMapper<TaskResponse> {

    private final JobResponseMapper jobResponseMapper;
    private final ContactResponseMapper contactResponseMapper;
    private final CustomFieldsMapper customFieldsMapper;

    @Override
    protected boolean processSpecialField(String fieldCaption, TaskResponse taskResponse, Map<String, Object> responseMap, Set<String> fieldList) {
        switch (fieldCaption) {
            case "contact" -> {
                if (taskResponse.getContact() != null) {
                    Map<String, Object> contactMap = contactResponseMapper.map(taskResponse.getContact(), fieldList);
                    responseMap.putAll(contactMap);
                }
                return true;
            }
            case "job" -> {
                if (taskResponse.getJob() != null) {
                    Map<String, Object> jobMap = jobResponseMapper.map(taskResponse.getJob(), fieldList);
                    responseMap.putAll(jobMap);
                }
                return true;
            }
            case "TaskCustomFieldsAndLists" -> {
                if (taskResponse.getCustomFieldsAndLists() != null) {
                    Map<String, String> customFieldsMap = customFieldsMapper.getFieldsMap(taskResponse.getCustomFieldsAndLists(), "Task", fieldList);
                    responseMap.putAll(customFieldsMap);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}