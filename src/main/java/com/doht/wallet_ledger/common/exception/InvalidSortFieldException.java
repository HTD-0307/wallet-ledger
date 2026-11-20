package com.doht.wallet_ledger.common.exception;

import com.doht.wallet_ledger.common.i18n.MessageKeys;
import java.util.Set;

public class InvalidSortFieldException extends DomainException {

    public InvalidSortFieldException(String sortBy, Set<String> allowedFields) {
        super(
                "INVALID_SORT_FIELD",
                MessageKeys.COMMON_ERROR_INVALID_SORT_FIELD,
                sortBy,
                allowedFields
        );
    }
}
