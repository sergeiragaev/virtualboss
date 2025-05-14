package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"entityId", "type"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Communication {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "type_id")
    @EqualsAndHashCode.Include
    private CommunicationType type;

    @Column(nullable = false, name = "entity_id")
    @EqualsAndHashCode.Include
    private UUID entityId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
