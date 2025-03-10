package net.virtualboss.common.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class GroupDto implements Serializable {
    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Name")
    private String name;

}
