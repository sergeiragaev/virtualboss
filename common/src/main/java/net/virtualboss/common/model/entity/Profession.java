package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "professions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Profession {

    @Id
    @GeneratedValue
    private UUID id;

    @EqualsAndHashCode.Include
    private String name;

    private String description;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
