package com.maroots.backend.email;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendInviteEmail(String toEmail, String tenantName, String role,String inviteLink){
        String subject = "You have been invited to join the tenant: " + tenantName;
        String text = String.format("Hello,\n\nYou've been invited to join %s as a %s.\nClick below to complete registration:\n\n%s",
                tenantName,role,inviteLink);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }
}
