package com.doht.wallet_ledger.auth.application.handler;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.UserProfileResponse;
import com.doht.wallet_ledger.auth.infrastructure.JwtTokenService;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenEntity;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenRepository;
import com.doht.wallet_ledger.auth.infrastructure.UserEntity;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AuthSessionIssuer {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    AuthResponse issueTokenPair(UserEntity user, Instant now) {
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String rawRefreshToken = jwtTokenService.generateRawRefreshToken();
        String refreshTokenHash = jwtTokenService.hashRefreshToken(rawRefreshToken);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.issue(
                user.getId(),
                refreshTokenHash,
                jwtTokenService.computeRefreshTokenExpiry(now)
        );
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.bearer(
                accessToken,
                rawRefreshToken,
                jwtTokenService.accessTokenExpiresInSeconds(),
                toProfile(user)
        );
    }

    static UserProfileResponse toProfile(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
