package com.maroots.backend.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String token;
    private String companyName;

}
