package net.virtualboss.task.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class EmployeeDto implements Serializable {
    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Name")
    private String name;

    @Builder.Default
    private String email = "";

    @Builder.Default
    private String color = "";

    @Builder.Default
    private String notes = "";

}
