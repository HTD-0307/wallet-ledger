package com.doht.wallet_ledger.auth.application.handler;

import com.doht.wallet_ledger.auth.application.dto.LogoutRequest;
import com.doht.wallet_ledger.auth.infrastructure.JwtTokenService;
import com.doht.wallet_ledger.auth.infrastructure.RefreshTokenRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;

    @Transactional
    public void handle(UUID currentUserId, LogoutRequest request) {
        String tokenHash = jwtTokenService.hashRefreshToken(request.refreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> token.getUserId().equals(currentUserId) && !token.isRevoked())
                .ifPresent(token -> {
                    token.revoke(clock.instant(), null);
                    refreshTokenRepository.save(token);
                    log.info("User logged out userId={}", currentUserId);
                });
    }
}
