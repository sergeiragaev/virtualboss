package net.virtualboss.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GroupDto {
    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Name")
    private String name;

}
