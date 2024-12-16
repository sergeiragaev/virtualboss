package net.virtualboss.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "fields")
@Getter
@Setter
public class Field  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_value", nullable = false)
    private String defaultValue;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, name = "\"order\"")
    private short order;

}
