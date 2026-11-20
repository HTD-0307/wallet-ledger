package com.doht.wallet_ledger.auth.application.dto;

import com.doht.wallet_ledger.common.i18n.MessageKeys;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "{" + MessageKeys.VALIDATION_AUTH_EMAIL_REQUIRED + "}")
        @Email(message = "{" + MessageKeys.VALIDATION_AUTH_EMAIL_INVALID + "}")
        @Size(max = 255, message = "{" + MessageKeys.VALIDATION_AUTH_EMAIL_MAX_LENGTH + "}")
        String email,

        @NotBlank(message = "{" + MessageKeys.VALIDATION_AUTH_PASSWORD_REQUIRED + "}")
        @Size(min = 8, max = 72, message = "{" + MessageKeys.VALIDATION_AUTH_PASSWORD_LENGTH + "}")
        @Pattern(
                regexp = "^(?=[^a-z]*[a-z])(?=[^A-Z]*[A-Z])(?=\\D*\\d).+$",
                message = "{" + MessageKeys.VALIDATION_AUTH_PASSWORD_PATTERN + "}"
        )
        String password,

        @NotBlank(message = "{" + MessageKeys.VALIDATION_AUTH_FULL_NAME_REQUIRED + "}")
        @Size(min = 2, max = 150, message = "{" + MessageKeys.VALIDATION_AUTH_FULL_NAME_LENGTH + "}")
        String fullName
) {
}
