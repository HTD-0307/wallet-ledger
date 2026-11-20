package com.doht.wallet_ledger.auth.domain.exception;

import com.doht.wallet_ledger.common.exception.DomainException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", MessageKeys.AUTH_ERROR_INVALID_CREDENTIALS);
    }
}
