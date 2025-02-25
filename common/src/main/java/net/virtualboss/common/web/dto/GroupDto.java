package net.virtualboss.common.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class GroupDto implements Serializable {
    @JsonProperty("Id")
    private Short id;

    @JsonProperty("Name")
    private String name;

}
