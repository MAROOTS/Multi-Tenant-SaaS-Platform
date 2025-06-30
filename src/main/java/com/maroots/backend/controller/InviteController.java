package com.maroots.backend.controller;
import com.maroots.backend.Repository.InviteRepository;
import com.maroots.backend.entity.Invite;
import com.maroots.backend.entity.TenantContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@AllArgsConstructor
@RequestMapping("/api/invites")
public class InviteController {
    private final InviteRepository inviteRepository;
    @GetMapping
    public ResponseEntity<?> validateInvite(@RequestParam("token") String token){
        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invite token"));

        if (invite.isUsed()){
            return ResponseEntity.badRequest().body("Invite already used!");
        }
        if (invite.getExpiryDate().isBefore(LocalDateTime.now())){
            return ResponseEntity.badRequest().body("Invite expired!");
        }
        Map<String,Object> response= new HashMap<>();
        response.put("email",invite.getEmail());
        response.put("role",invite.getRole());
        response.put("tenant",invite.getTenantIdentifier());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Invite>> getInvites(){
        String tenantId = TenantContextHolder.getTenantId();
        List<Invite> invites = inviteRepository.findAllByTenantIdentifier(tenantId);
        return ResponseEntity.ok(invites);
    }

}
