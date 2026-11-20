package com.doht.wallet_ledger.auth.application.handler;

import com.doht.wallet_ledger.auth.application.dto.UserProfileResponse;
import com.doht.wallet_ledger.auth.domain.exception.UserNotFoundException;
import com.doht.wallet_ledger.auth.infrastructure.UserEntity;
import com.doht.wallet_ledger.auth.infrastructure.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserHandler {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse handle(UUID currentUserId) {
        UserEntity user = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(UserNotFoundException::new);
        return AuthSessionIssuer.toProfile(user);
    }
}
