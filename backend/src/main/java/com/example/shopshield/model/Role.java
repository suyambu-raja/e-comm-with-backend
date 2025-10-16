package com.example.shopshield.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "roles", schema = "backend")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    private ERole name;

    public Role() {}

    public Role(ERole name) {
        this.name = name;
    }
}