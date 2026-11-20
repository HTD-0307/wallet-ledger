package com.doht.wallet_ledger.auth.application.handler;

import com.doht.wallet_ledger.auth.application.dto.AuthResponse;
import com.doht.wallet_ledger.auth.application.dto.RegisterRequest;
import com.doht.wallet_ledger.auth.domain.exception.EmailAlreadyExistsException;
import com.doht.wallet_ledger.auth.infrastructure.UserEntity;
import com.doht.wallet_ledger.auth.infrastructure.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionIssuer sessionIssuer;
    private final Clock clock;

    @Transactional
    public AuthResponse handle(RegisterRequest request) {
        String normalizedEmail = AuthSessionIssuer.normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        UserEntity user = UserEntity.register(normalizedEmail, passwordHash, request.fullName().trim());

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException _) {
            throw new EmailAlreadyExistsException();
        }

        log.info("Registered new user account userId={}", user.getId());
        return sessionIssuer.issueTokenPair(user, clock.instant());
    }
}
