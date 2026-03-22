-- Usuário de desenvolvimento (5000 créditos). Remova ou ajuste em produção.
INSERT INTO users (id, supabase_user_id, email, credit_balance, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'dev-local-supabase-id',
    'dev@leadmaps.local',
    5000,
    NOW(),
    NOW()
) ON CONFLICT (supabase_user_id) DO NOTHING;
