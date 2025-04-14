package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.mapper.v1.AbstractResponseMapper;
import net.virtualboss.common.mapper.v1.CustomFieldsMapper;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ContactResponseMapper extends AbstractResponseMapper<ContactResponse> {

    private final CustomFieldsMapper customFieldsMapper;

    public ContactResponseMapper(CustomFieldsMapper customFieldsMapper) {
        this.customFieldsMapper = customFieldsMapper;
    }

    @Override
    protected boolean processSpecialField(String fieldCaption, ContactResponse contactResponse, Map<String, Object> responseMap, Set<String> fieldList) {
        if ("ContactPerson".equals(fieldCaption)) {
            String person = contactResponse.getPerson();
            if (person != null) {
                responseMap.put("ContactPerson", person);
            }
            return true;
        }
        if ("ContactCustomFieldsAndLists".equals(fieldCaption)) {
            CustomFieldsAndLists customFields = contactResponse.getCustomFieldsAndLists();
            if (customFields != null) {
                Map<String, String> customFieldsMap = customFieldsMapper.getFieldsMap(customFields, "Contact", fieldList);
                responseMap.putAll(customFieldsMap);
            }
            return true;
        }
        return false;
    }
}
