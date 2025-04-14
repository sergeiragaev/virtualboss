package net.virtualboss.common.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomValueDto {
    private Long id;
    private Integer fieldId;
    private String fieldValue;
}
