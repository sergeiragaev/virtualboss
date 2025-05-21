package net.virtualboss.contact.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionResponse {
    @JsonProperty("ProfessionId")
    @EntityMapping
    private UUID id;

    @JsonProperty("ContactProfession")
    @Builder.Default
    @EntityMapping
    private String name = "";

}
