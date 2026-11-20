package com.doht.wallet_ledger.common.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final String messageKey;
    private final transient Object[] messageArgs;

    protected DomainException(String errorCode, String messageKey, Object... messageArgs) {
        super(messageKey);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    protected DomainException(String errorCode, String messageKey, Throwable cause, Object... messageArgs) {
        super(messageKey, cause);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public Object[] getMessageArgs() {
        return messageArgs == null ? new Object[0] : messageArgs.clone();
    }
}
