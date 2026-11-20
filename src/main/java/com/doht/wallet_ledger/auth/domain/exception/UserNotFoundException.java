package com.doht.wallet_ledger.auth.domain.exception;

import com.doht.wallet_ledger.common.exception.DomainException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("USER_NOT_FOUND", MessageKeys.AUTH_ERROR_USER_NOT_FOUND);
    }
}
