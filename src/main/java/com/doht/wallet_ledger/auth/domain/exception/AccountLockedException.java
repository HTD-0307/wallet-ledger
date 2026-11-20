package com.doht.wallet_ledger.auth.domain.exception;

import com.doht.wallet_ledger.common.exception.DomainException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;

public class AccountLockedException extends DomainException {

    private AccountLockedException(String messageKey) {
        super("ACCOUNT_LOCKED", messageKey);
    }

    public static AccountLockedException permanent() {
        return new AccountLockedException(MessageKeys.AUTH_ERROR_ACCOUNT_LOCKED_PERMANENT);
    }

    public static AccountLockedException temporary() {
        return new AccountLockedException(MessageKeys.AUTH_ERROR_ACCOUNT_LOCKED_TEMPORARY);
    }
}
