package com.doht.wallet_ledger.auth.application.handler;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.LoginRequest;
import com.doht.wallet_ledger.auth.domain.exception.AccountLockedException;
import com.doht.wallet_ledger.auth.domain.exception.InvalidCredentialsException;
import com.doht.wallet_ledger.auth.infrastructure.UserEntity;
import com.doht.wallet_ledger.auth.infrastructure.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHandler {

    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionIssuer sessionIssuer;
    private final Clock clock;

    // Sinh hash giả lập lúc chạy để cân bằng thời gian phản hồi, tránh hardcode chuỗi hash trong mã nguồn
    private volatile String dummyPasswordHash;

    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})
    public AuthResponse handle(LoginRequest request) {
        String normalizedEmail = AuthSessionIssuer.normalizeEmail(request.email());
        Instant now = clock.instant();

        UserEntity user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)
                .orElse(null);

        if (user == null) {
            // Chạy so khớp mật khẩu giả để chống tấn công đo thời gian (account enumeration)
            boolean _ = passwordEncoder.matches(request.password(), getDummyPasswordHash());
            throw new InvalidCredentialsException();
        }

        if (user.isPermanentlyBlocked()) {
            throw AccountLockedException.permanent();
        }

        if (user.isTemporarilyLockedAt(now)) {
            throw AccountLockedException.temporary();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.recordFailedLogin(now, MAX_FAILED_LOGIN_ATTEMPTS, LOCK_DURATION);
            userRepository.save(user);
            log.warn("Failed login attempt userId={} attempts={}", user.getId(), user.getFailedLoginAttempts());
            throw new InvalidCredentialsException();
        }

        user.recordSuccessfulLogin(now);
        userRepository.save(user);
        log.info("User logged in userId={}", user.getId());
        return sessionIssuer.issueTokenPair(user, now);
    }

    private String getDummyPasswordHash() {
        String cached = this.dummyPasswordHash;
        if (cached == null) {
            cached = passwordEncoder.encode(UUID.randomUUID().toString());
            this.dummyPasswordHash = cached;
        }
        return cached;
    }
}
