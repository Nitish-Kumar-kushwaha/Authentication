CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_id UUID NULL REFERENCES "users"(id) ON DELETE CASCADE,
    admin_user_id UUID NULL REFERENCES "admin_users"(id) ON DELETE CASCADE,
    token VARCHAR(256) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT ck_refresh_tokens_one_owner CHECK (
        (user_id IS NOT NULL AND admin_user_id IS NULL)
        OR
        (user_id IS NULL AND admin_user_id IS NOT NULL)
    )
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);

