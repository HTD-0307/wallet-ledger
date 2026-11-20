package com.doht.wallet_ledger.auth.application.handler;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.RefreshTokenRequest;
import com.doht.wallet_ledger.auth.domain.exception.AccountLockedException;
import com.doht.wallet_ledger.auth.domain.exception.InvalidRefreshTokenException;
import com.doht.wallet_ledger.auth.infrastructure.JwtTokenService;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenEntity;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenRepository;
import com.doht.wallet_ledger.auth.infrastructure.UserEntity;
import com.doht.wallet_ledger.auth.infrastructure.UserRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResponse handle(RefreshTokenRequest request) {
        Instant now = clock.instant();
        String incomingHash = jwtTokenService.hashRefreshToken(request.refreshToken());

        RefreshTokenEntity existingToken = refreshTokenRepository.findByTokenHash(incomingHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (existingToken.isRevoked()) {
            refreshTokenRepository.revokeAllActiveByUserId(existingToken.getUserId(), now);
            log.warn("Refresh token reuse detected for userId={}; revoked all active sessions", existingToken.getUserId());
            throw new InvalidRefreshTokenException();
        }

        if (existingToken.isExpiredAt(now)) {
            existingToken.revoke(now, null);
            refreshTokenRepository.save(existingToken);
            throw new InvalidRefreshTokenException();
        }

        UserEntity user = userRepository.findByIdAndDeletedFalse(existingToken.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        if (user.isPermanentlyBlocked() || user.isTemporarilyLockedAt(now)) {
            throw AccountLockedException.permanent();
        }

        String newRawRefreshToken = jwtTokenService.generateRawRefreshToken();
        String newRefreshTokenHash = jwtTokenService.hashRefreshToken(newRawRefreshToken);

        existingToken.revoke(now, newRefreshTokenHash);
        refreshTokenRepository.save(existingToken);

        RefreshTokenEntity replacement = RefreshTokenEntity.issue(
                user.getId(),
                newRefreshTokenHash,
                jwtTokenService.computeRefreshTokenExpiry(now)
        );
        refreshTokenRepository.save(replacement);

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.bearer(
                accessToken,
                newRawRefreshToken,
                jwtTokenService.accessTokenExpiresInSeconds(),
                AuthSessionIssuer.toProfile(user)
        );
    }
}
