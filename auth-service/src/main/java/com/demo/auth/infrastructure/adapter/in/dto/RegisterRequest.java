package com.demo.auth.infrastructure.adapter.in.dto;

public class RegisterRequest {
    private String email;

    public RegisterRequest() {}

    public String getEmail()             { return email; }
    public void   setEmail(String email) { this.email = email; }
}
