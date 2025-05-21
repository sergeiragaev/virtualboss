package net.virtualboss.contact.mapper.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.mapper.v1.AbstractResponseMapper;
import net.virtualboss.common.mapper.v1.CustomFieldsMapper;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ContactResponseMapper extends AbstractResponseMapper<ContactResponse> {

    private final CustomFieldsMapper customFieldsMapper;
    private final CompanyResponseMapper companyResponseMapper;
    private final ProfessionResponseMapper professionResponseMapper;

    @Override
    protected boolean processSpecialField(String fieldCaption, ContactResponse contactResponse, Map<String, Object> responseMap, Set<String> fieldList) {
        switch (fieldCaption) {
            case "ContactPerson" -> {
                String person = contactResponse.getPerson();
                if (person != null) {
                    responseMap.put("ContactPerson", person);
                }
                return true;
            }
            case "company" -> {
                if (contactResponse.getCompany() != null) {
                    Map<String, Object> contactMap = companyResponseMapper.map(contactResponse.getCompany(), fieldList);
                    responseMap.putAll(contactMap);
                }
                return true;
            }
            case "profession" -> {
                if (contactResponse.getProfession() != null) {
                    Map<String, Object> contactMap = professionResponseMapper.map(contactResponse.getProfession(), fieldList);
                    responseMap.putAll(contactMap);
                }
                return true;
            }
            case "ContactCustomFieldsAndLists" -> {
                CustomFieldsAndLists customFields = contactResponse.getCustomFieldsAndLists();
                if (customFields != null) {
                    Map<String, String> customFieldsMap = customFieldsMapper.getFieldsMap(customFields, "Contact", fieldList);
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
