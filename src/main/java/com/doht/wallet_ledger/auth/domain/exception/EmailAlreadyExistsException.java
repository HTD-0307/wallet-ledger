package com.doht.wallet_ledger.auth.domain.exception;

import com.doht.wallet_ledger.common.exception.DomainException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS", MessageKeys.AUTH_ERROR_EMAIL_ALREADY_EXISTS);
    }
}
