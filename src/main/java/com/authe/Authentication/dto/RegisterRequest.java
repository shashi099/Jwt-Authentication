package com.authe.Authentication.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String role;  // Pass "USER" or "ADMIN" from frontend
}
