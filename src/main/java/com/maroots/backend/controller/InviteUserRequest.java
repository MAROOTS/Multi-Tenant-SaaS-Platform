package com.maroots.backend.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteUserRequest {
    private String name;
    private String email;
    private String password;
    private String role;
}
