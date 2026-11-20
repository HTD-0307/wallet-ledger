package com.doht.wallet_ledger.auth.infrastructure;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {

    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        issuer = (issuer == null || issuer.isBlank()) ? "wallet-ledger" : issuer;
        accessTokenTtl = (accessTokenTtl == null || accessTokenTtl.isNegative() || accessTokenTtl.isZero())
                ? Duration.ofMinutes(15)
                : accessTokenTtl;
        refreshTokenTtl = (refreshTokenTtl == null || refreshTokenTtl.isNegative() || refreshTokenTtl.isZero())
                ? Duration.ofDays(7)
                : refreshTokenTtl;
    }
}
