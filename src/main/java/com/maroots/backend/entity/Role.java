package com.maroots.backend.entity;

import jakarta.persistence.*;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private RoleName name;

    public RoleName getName() {
    return name;
}

public void setName(RoleName name) {
    this.name = name;
}

}
