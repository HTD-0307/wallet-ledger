package com.doht.wallet_ledger.auth.domain.exception;

import com.doht.wallet_ledger.common.exception.DomainException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;

public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super("INVALID_REFRESH_TOKEN", MessageKeys.AUTH_ERROR_INVALID_REFRESH_TOKEN);
    }
}
