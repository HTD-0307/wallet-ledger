package com.doht.wallet_ledger.auth.application.dto;

import com.doht.wallet_ledger.common.i18n.MessageKeys;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "{" + MessageKeys.VALIDATION_AUTH_EMAIL_REQUIRED + "}")
        @Email(message = "{" + MessageKeys.VALIDATION_AUTH_EMAIL_INVALID + "}")
        @Size(max = 255, message = "{" + MessageKeys.VALIDATION_AUTH_EMAIL_MAX_LENGTH + "}")
        String email,

        @NotBlank(message = "{" + MessageKeys.VALIDATION_AUTH_PASSWORD_REQUIRED + "}")
        @Size(max = 72, message = "{" + MessageKeys.VALIDATION_AUTH_PASSWORD_MAX_LENGTH + "}")
        String password
) {
}
