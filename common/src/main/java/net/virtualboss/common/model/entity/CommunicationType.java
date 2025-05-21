package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;
import net.virtualboss.common.model.enums.ChannelType;

import java.util.UUID;

@Entity
@Table(name = "communication_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CommunicationType {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private ChannelType channel;

    @EqualsAndHashCode.Include
    private String caption;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
