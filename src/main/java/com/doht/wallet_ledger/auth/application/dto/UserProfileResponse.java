package com.doht.wallet_ledger.auth.application.dto;

import com.doht.wallet_ledger.auth.domain.UserRole;
import com.doht.wallet_ledger.auth.domain.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {
}
