package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "custom_values")
@Getter
public class FieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false)
//    @Enumerated(EnumType.STRING)
//    private OwnerType owner;

    @ManyToOne
    @JoinColumn(nullable = false, name = "field_id", referencedColumnName = "id")
    private Field field;

    @Column(name = "\"value\"", nullable = false)
    private String value;
}
