package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"entityId", "type"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "type_id")
    private CommunicationType type;

    @Column(nullable = false, name = "entity_id")
    private UUID entityId;

    private String address1;

    private String address2;

    private String city;

    private String state;

    private String postal;

    private String country;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
