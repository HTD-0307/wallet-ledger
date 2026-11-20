package com.doht.wallet_ledger.auth.application.dto;

import com.doht.wallet_ledger.common.i18n.MessageKeys;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "{" + MessageKeys.VALIDATION_AUTH_REFRESH_TOKEN_REQUIRED + "}")
        String refreshToken
) {
}
