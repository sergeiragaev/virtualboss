package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "custom_values")
@Getter
@Setter
public class FieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "field_id", referencedColumnName = "id")
    private Field field;

    @Column(name = "custom_value", nullable = false)
    @Builder.Default
    private String customValue = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldValue that = (FieldValue) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field);
    }
}
