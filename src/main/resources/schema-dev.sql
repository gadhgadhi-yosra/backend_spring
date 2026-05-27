ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS category_key VARCHAR(255);
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS image_url VARCHAR(1200);
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS sort_order INTEGER;
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS featured_promo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS featured_bio BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS featured_new BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS featured_popular BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS custom_tags VARCHAR(2000);

ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS featured_promo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS featured_bio BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS featured_new BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS featured_popular BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS custom_tags VARCHAR(2000);

ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12, 2);

UPDATE orders
SET total_amount = COALESCE(total_amount, total)
WHERE total_amount IS NULL;

ALTER TABLE IF EXISTS orders ALTER COLUMN total_amount SET NOT NULL;
ALTER TABLE IF EXISTS orders ALTER COLUMN total DROP NOT NULL;

UPDATE categories
SET display_name = COALESCE(display_name, name);

UPDATE categories
SET sort_order = COALESCE(sort_order, 0);

UPDATE categories
SET category_key = COALESCE(
    category_key,
    NULLIF(
        TRIM(BOTH '-' FROM REGEXP_REPLACE(LOWER(display_name), '[^a-z0-9]+', '-', 'g')),
        ''
    )
);

CREATE TABLE IF NOT EXISTS in_app_notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    order_reference VARCHAR(32),
    unread BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_in_app_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_in_app_notifications_user_created_at
    ON in_app_notifications(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS user_device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_device_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_device_tokens_user_id
    ON user_device_tokens(user_id);

-- Legacy BYTEA -> VARCHAR repair is handled at runtime by DatabaseRepair.java.
