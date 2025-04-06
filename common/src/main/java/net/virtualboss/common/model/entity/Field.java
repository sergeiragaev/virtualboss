package net.virtualboss.common.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fields")
@Getter
@Setter
public class Field {
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

    @Column(nullable = false, name = "field_order")
    private Short fieldOrder;

    @Column(nullable = false)
    private String path;
}
