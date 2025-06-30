package com.maroots.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
@Entity
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String companyName;

    @Column(unique = true)
    private String tenantIdentifier;
    @OneToMany(mappedBy = "tenant")
    private List<AppUser> users;
    private boolean active;
}
