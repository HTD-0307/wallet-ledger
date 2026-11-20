package com.doht.wallet_ledger.common.i18n;

/**
 * Tập trung toàn bộ khóa thông báo được phân giải qua {@link org.springframework.context.MessageSource}
 * ({@code messages.properties} và các biến thể ngôn ngữ như {@code messages_vi.properties}).
 */
public final class MessageKeys {

    private MessageKeys() {
    }

    // --- Common Error Titles & Details ---
    public static final String COMMON_ERROR_INVALID_REQUEST_TITLE = "common.error.invalid_request.title";
    public static final String COMMON_ERROR_VALIDATION_FAILED_TITLE = "common.error.validation_failed.title";
    public static final String COMMON_ERROR_VALIDATION_FAILED_DETAIL = "common.error.validation_failed.detail";
    public static final String COMMON_ERROR_CONSTRAINT_VIOLATION_DETAIL = "common.error.constraint_violation.detail";
    public static final String COMMON_ERROR_INVALID_VALUE = "common.error.invalid_value";
    public static final String COMMON_ERROR_INVALID_SORT_FIELD = "common.error.invalid_sort_field";
    public static final String COMMON_ERROR_CONCURRENT_MODIFICATION_TITLE = "common.error.concurrent_modification.title";
    public static final String COMMON_ERROR_CONCURRENT_MODIFICATION_DETAIL = "common.error.concurrent_modification.detail";
    public static final String COMMON_ERROR_DATA_CONFLICT_TITLE = "common.error.data_conflict.title";
    public static final String COMMON_ERROR_DATA_CONFLICT_DETAIL = "common.error.data_conflict.detail";
    public static final String COMMON_ERROR_INTERNAL_SERVER_TITLE = "common.error.internal_server.title";
    public static final String COMMON_ERROR_INTERNAL_SERVER_DETAIL = "common.error.internal_server.detail";
    public static final String COMMON_ERROR_UNAUTHORIZED_TITLE = "common.error.unauthorized.title";
    public static final String COMMON_ERROR_UNAUTHORIZED_DETAIL = "common.error.unauthorized.detail";
    public static final String COMMON_ERROR_FORBIDDEN_TITLE = "common.error.forbidden.title";
    public static final String COMMON_ERROR_FORBIDDEN_DETAIL = "common.error.forbidden.detail";
    public static final String COMMON_ERROR_CONFLICT_TITLE = "common.error.conflict.title";
    public static final String COMMON_ERROR_NOT_FOUND_TITLE = "common.error.not_found.title";

    // --- Auth Domain Errors ---
    public static final String AUTH_ERROR_EMAIL_ALREADY_EXISTS = "auth.error.email_already_exists";
    public static final String AUTH_ERROR_INVALID_CREDENTIALS = "auth.error.invalid_credentials";
    public static final String AUTH_ERROR_ACCOUNT_LOCKED_TITLE = "auth.error.account_locked.title";
    public static final String AUTH_ERROR_ACCOUNT_LOCKED_PERMANENT = "auth.error.account_locked.permanent";
    public static final String AUTH_ERROR_ACCOUNT_LOCKED_TEMPORARY = "auth.error.account_locked.temporary";
    public static final String AUTH_ERROR_INVALID_REFRESH_TOKEN = "auth.error.invalid_refresh_token";
    public static final String AUTH_ERROR_USER_NOT_FOUND = "auth.error.user_not_found";

    // --- Auth Bean Validation ---
    public static final String VALIDATION_AUTH_EMAIL_REQUIRED = "validation.auth.email.required";
    public static final String VALIDATION_AUTH_EMAIL_INVALID = "validation.auth.email.invalid";
    public static final String VALIDATION_AUTH_EMAIL_MAX_LENGTH = "validation.auth.email.max_length";
    public static final String VALIDATION_AUTH_PASSWORD_REQUIRED = "validation.auth.password.required";
    public static final String VALIDATION_AUTH_PASSWORD_LENGTH = "validation.auth.password.length";
    public static final String VALIDATION_AUTH_PASSWORD_MAX_LENGTH = "validation.auth.password.max_length";
    public static final String VALIDATION_AUTH_PASSWORD_PATTERN = "validation.auth.password.pattern";
    public static final String VALIDATION_AUTH_FULL_NAME_REQUIRED = "validation.auth.full_name.required";
    public static final String VALIDATION_AUTH_FULL_NAME_LENGTH = "validation.auth.full_name.length";
    public static final String VALIDATION_AUTH_REFRESH_TOKEN_REQUIRED = "validation.auth.refresh_token.required";
}
