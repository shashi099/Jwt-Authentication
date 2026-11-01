package com.authe.Authentication.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
