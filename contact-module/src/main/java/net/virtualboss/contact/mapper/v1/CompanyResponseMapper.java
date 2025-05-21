package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.mapper.v1.AbstractResponseMapper;
import net.virtualboss.contact.web.dto.CompanyResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class CompanyResponseMapper extends AbstractResponseMapper<CompanyResponse> {

    @Override
    protected boolean processSpecialField(String fieldCaption, CompanyResponse companyResponse, Map<String, Object> responseMap, Set<String> fieldList) {
        return false;
    }
}
