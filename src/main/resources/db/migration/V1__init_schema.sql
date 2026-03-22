-- Schema alinhado ao modelo Prisma (User, SearchHistory, Lead, Subscription) — PostgreSQL / Supabase

CREATE TABLE users (
    id UUID PRIMARY KEY,
    supabase_user_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    credit_balance INTEGER NOT NULL DEFAULT 0,
    stripe_customer_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_supabase_user_id ON users (supabase_user_id);

CREATE TABLE search_histories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    keyword VARCHAR(512) NOT NULL,
    location VARCHAR(512) NOT NULL,
    full_query VARCHAR(1024) NOT NULL,
    status VARCHAR(32) NOT NULL,
    leads_found INTEGER NOT NULL DEFAULT 0,
    credits_charged INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_histories_user_id ON search_histories (user_id);
CREATE INDEX idx_search_histories_created_at ON search_histories (created_at DESC);

CREATE TABLE leads (
    id UUID PRIMARY KEY,
    search_history_id UUID NOT NULL REFERENCES search_histories (id) ON DELETE CASCADE,
    name VARCHAR(512),
    phone VARCHAR(128),
    website VARCHAR(2048),
    rating NUMERIC(4, 2),
    review_count INTEGER,
    instagram_url VARCHAR(2048),
    facebook_url VARCHAR(2048),
    maps_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leads_search_history_id ON leads (search_history_id);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    stripe_subscription_id VARCHAR(255),
    plan_code VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_period_end TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);
CREATE INDEX idx_subscriptions_stripe ON subscriptions (stripe_subscription_id);
