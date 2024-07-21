package com.movieflex.auth.utlis;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
