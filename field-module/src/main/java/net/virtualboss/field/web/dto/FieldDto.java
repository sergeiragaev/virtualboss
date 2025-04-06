package net.virtualboss.field.web.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FieldDto {

    private String name;
    private String defaultValue;
    private String alias;
    private boolean enabled;
    private short fieldOrder;

}
