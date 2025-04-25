package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "contact_phones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"contact", "phoneType"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Phone {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "type_id")
    private PhoneType phoneType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "contact_id")
    private Contact contact;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
