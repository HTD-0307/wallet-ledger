CREATE TABLE users (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    full_name             VARCHAR(150) NOT NULL,
    role                  VARCHAR(32)  NOT NULL DEFAULT 'USER',
    status                VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    version               BIGINT       NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ  NOT NULL,
    created_by            UUID,
    updated_at            TIMESTAMPTZ,
    updated_by            UUID,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMPTZ,
    deleted_by            UUID,
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'LOCKED', 'SUSPENDED'))
);

CREATE UNIQUE INDEX ux_users_email_active
    ON users (LOWER(email))
    WHERE deleted = FALSE;

CREATE TABLE refresh_tokens (
    id                     UUID PRIMARY KEY,
    user_id                UUID         NOT NULL REFERENCES users(id),
    token_hash             VARCHAR(64)  NOT NULL,
    expires_at             TIMESTAMPTZ  NOT NULL,
    revoked_at             TIMESTAMPTZ,
    replaced_by_token_hash VARCHAR(64),
    created_at             TIMESTAMPTZ  NOT NULL,
    created_by             UUID,
    CONSTRAINT ux_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_tokens_user_expires
    ON refresh_tokens (user_id, expires_at);
