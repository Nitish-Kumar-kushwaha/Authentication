CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(255)
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_permissions_name ON permissions(name);

