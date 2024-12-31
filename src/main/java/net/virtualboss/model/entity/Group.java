package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.model.enums.EntityType;

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
