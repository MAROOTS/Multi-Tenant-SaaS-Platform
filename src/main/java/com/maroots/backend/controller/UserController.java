package com.maroots.backend.controller;

import com.maroots.backend.Repository.InviteRepository;
import com.maroots.backend.Repository.RoleRepository;
import com.maroots.backend.Repository.TenantRepository;
import com.maroots.backend.Repository.UserRepository;
import com.maroots.backend.email.EmailService;
import com.maroots.backend.entity.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final InviteRepository inviteRepository;
    @Value("${frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostConstruct
    private void validateFrontendUrl() {
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            throw new IllegalStateException("frontend.url must be configured");
        }
        try {
            new URL(frontendUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid frontend.url configuration", e);
        }
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteUserRequest request) throws MalformedURLException {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("User already exists with the email!");
        }

        String tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdentifier(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found with tenantId: " + tenantId));

        RoleName roleName;
        try {
            roleName = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role name!");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(3);
        Invite invite = new Invite();
        invite.setEmail(request.getEmail());
        invite.setToken(token);
        invite.setRole(role.getName().name());
        invite.setTenantIdentifier(tenant.getTenantIdentifier());
        invite.setExpiryDate(expiryDate);
        inviteRepository.save(invite);
        String inviteLink = UriComponentsBuilder.newInstance()
                .scheme(new URL(frontendUrl).getProtocol())
                .host(new URL(frontendUrl).getHost())
                .port(new URL(frontendUrl).getPort())
                .path("/invite/register?token=" + token)
                .queryParam("email", request.getEmail())
                .queryParam("tenant", tenantId)
                .queryParam("role", roleName.name())
                .build()
                .encode()
                .toUriString();

        emailService.sendInviteEmail(request.getEmail(), tenant.getCompanyName(), roleName.name(), inviteLink);

        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenant(tenant);
        user.setRoles(Set.of(role));
        userRepository.save(user);

        return ResponseEntity.ok("Invite email sent to " + request.getEmail());
    }
}