package com.maroots.backend.controller;

import com.maroots.backend.Repository.RoleRepository;
import com.maroots.backend.Repository.TenantRepository;
import com.maroots.backend.Repository.UserRepository;
import com.maroots.backend.entity.Role;
import com.maroots.backend.entity.RoleName;
import com.maroots.backend.entity.Tenant;
import com.maroots.backend.entity.AppUser;
import com.maroots.backend.jwt.CustomUserDetails;
import com.maroots.backend.jwt.CustomUserDetailsService;
import com.maroots.backend.jwt.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String tenantId = ((CustomUserDetails) userDetails).getTenantId();
        String token = jwtUtil.generateToken(userDetails,tenantId);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            return ResponseEntity.badRequest().body("Email is already in use!");
        }
        Tenant tenant = new Tenant();
        tenant.setCompanyName(request.getCompanyName());
        tenant.setTenantIdentifier(UUID.randomUUID().toString());
        tenantRepository.save(tenant);

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenant(tenant);
      Role role = roleRepository.findByName(RoleName.ADMIN)
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName(RoleName.ADMIN);
                return roleRepository.save(newRole);
            });

        user.setRoles(Set.of(role));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully with tenant" + tenant.getTenantIdentifier());
    }
}
