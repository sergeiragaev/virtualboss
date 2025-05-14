package net.virtualboss.job.mapper.v1;

import lombok.AllArgsConstructor;
import net.virtualboss.common.mapper.v1.AbstractResponseMapper;
import net.virtualboss.common.mapper.v1.CustomFieldsMapper;
import net.virtualboss.contact.mapper.v1.ContactResponseMapper;
import net.virtualboss.job.web.dto.JobResponse;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public class JobResponseMapper extends AbstractResponseMapper<JobResponse> {

    private final CustomFieldsMapper customFieldsMapper;
    private final ContactResponseMapper contactResponseMapper;


    @Override
    protected boolean processSpecialField(String fieldCaption, JobResponse jobResponse, Map<String, Object> responseMap, Set<String> fieldList) {
        if ("JobCustomFieldsAndLists".equals(fieldCaption)) {
            CustomFieldsAndLists customFields = jobResponse.getCustomFieldsAndLists();
            if (customFields != null) {
                Map<String, String> customFieldsMap = customFieldsMapper.getFieldsMap(customFields, "Job", fieldList);
                responseMap.putAll(customFieldsMap);
            }
            return true;
        } else if ("owner".equals(fieldCaption)) {
            if (jobResponse.getOwner() != null) {
                Map<String, Object> contactMap = contactResponseMapper.map(jobResponse.getOwner(), fieldList);
                responseMap.putAll(contactMap);
            }
            return true;
        }

        return false;
    }
}