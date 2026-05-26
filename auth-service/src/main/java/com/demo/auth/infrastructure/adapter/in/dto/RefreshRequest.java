package com.demo.auth.infrastructure.adapter.in.dto;

public class RefreshRequest {
    private String refreshToken;

    public RefreshRequest() {}

    public String getRefreshToken()                    { return refreshToken; }
    public void   setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
