package com.maroots.backend.Repository;

import com.maroots.backend.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByTenantIdentifier(String tenantIdentifier);
}
