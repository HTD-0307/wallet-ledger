package com.doht.wallet_ledger.auth.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user
) {

    public static AuthResponse bearer(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserProfileResponse user
    ) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
