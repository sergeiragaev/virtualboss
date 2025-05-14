package net.virtualboss.contact.mapper.v1;

import net.virtualboss.common.mapper.v1.AbstractResponseMapper;
import net.virtualboss.contact.web.dto.ProfessionResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ProfessionResponseMapper extends AbstractResponseMapper<ProfessionResponse> {

    @Override
    protected boolean processSpecialField(String fieldCaption, ProfessionResponse professionResponse, Map<String, Object> responseMap, Set<String> fieldList) {
        return false;
    }
}
