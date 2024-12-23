package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Entity
@Table(name = "fields")
@Getter
@Setter
public class Field implements Supplier<Field> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_value", nullable = false)
    private String defaultValue;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, name = "\"order\"")
    private Short order;

    @Override
    public Field get() {
        return this;
    }
}
