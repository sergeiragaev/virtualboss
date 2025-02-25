package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.common.model.enums.EntityType;

@Entity
@Table(name = "groups")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;
    @Enumerated(EnumType.STRING)
    private EntityType type;
    private String name;
    private String description;
}
