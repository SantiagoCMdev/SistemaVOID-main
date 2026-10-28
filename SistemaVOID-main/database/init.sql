-- =========================================================================
-- DATABASE INITIALIZATION & CONSTRAINTS SCRIPT (POSTGRESQL ACID)
-- =========================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. TIPOS ENUMERADOS
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE subscription_status AS ENUM ('INCOMPLETE', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'UNPAID');
CREATE TYPE billing_interval AS ENUM ('MONTHLY', 'YEARLY');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED');

-- 2. TABLA: USUARIOS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role user_role DEFAULT 'USER' NOT NULL,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. TABLA: PLANES
CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50) UNIQUE NOT NULL,
    price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    interval billing_interval DEFAULT 'MONTHLY' NOT NULL,
    max_concurrent_streams INTEGER DEFAULT 1 NOT NULL CHECK (max_concurrent_streams > 0),
    max_resolution VARCHAR(10) DEFAULT '1080p' NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 4. TABLA: SUSCRIPCIONES
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE RESTRICT,
    status subscription_status DEFAULT 'INCOMPLETE' NOT NULL,
    current_period_start TIMESTAMPTZ(6) NOT NULL,
    current_period_end TIMESTAMPTZ(6) NOT NULL,
    cancel_at_period_end BOOLEAN DEFAULT false NOT NULL,
    canceled_at TIMESTAMPTZ(6),
    pending_downgrade_plan_id UUID REFERENCES plans(id) ON DELETE SET NULL,
    payment_gateway_sub_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- REGLA DE CONSISTENCIA ACID:
-- Garantiza que un usuario JAMÁS tenga más de una suscripción 'ACTIVE' simultáneamente a nivel de motor de BD.
CREATE UNIQUE INDEX unique_active_subscription_per_user 
ON subscriptions (user_id) 
WHERE status = 'ACTIVE';

-- 5. TABLA: FACTURAS / TRANSACCIONES IDEMPOTENTES
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE SET NULL,
    idempotency_key VARCHAR(128) UNIQUE NOT NULL,
    amount_cents INTEGER NOT NULL CHECK (amount_cents >= 0),
    currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    status payment_status DEFAULT 'PENDING' NOT NULL,
    payment_gateway_ref VARCHAR(255),
    billing_reason VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. TABLA: VIDEOS / MEDIA
CREATE TABLE videos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    release_year INTEGER NOT NULL,
    age_rating VARCHAR(10) DEFAULT 'PG-13' NOT NULL,
    thumbnail_url VARCHAR(500) NOT NULL,
    poster_url VARCHAR(500) NOT NULL,
    hls_master_playlist_url VARCHAR(500) NOT NULL,
    is_published BOOLEAN DEFAULT false NOT NULL,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 7. TABLA: METADATOS DE CATÁLOGO (Para indexación e índices GIN)
CREATE TABLE video_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    video_id UUID UNIQUE NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    genres TEXT[] NOT NULL,
    actors TEXT[] NOT NULL,
    directors TEXT[] NOT NULL,
    original_language VARCHAR(10) DEFAULT 'es' NOT NULL,
    available_languages TEXT[] DEFAULT ARRAY['es', 'en'] NOT NULL,
    subtitles TEXT[] DEFAULT ARRAY['es', 'en'] NOT NULL
);

-- Índices GIN para búsquedas y filtros en tiempo real sobre arrays
CREATE INDEX idx_video_metadata_genres ON video_metadata USING GIN (genres);
CREATE INDEX idx_video_metadata_actors ON video_metadata USING GIN (actors);
CREATE INDEX idx_video_metadata_directors ON video_metadata USING GIN (directors);

-- 8. TABLA: PROGRESO DE REPRODUCCIÓN (Sincronización exacta)
CREATE TABLE playback_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    last_position_seconds INTEGER DEFAULT 0 NOT NULL,
    total_duration_seconds INTEGER NOT NULL,
    completed BOOLEAN DEFAULT false NOT NULL,
    updated_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_video_progress UNIQUE(user_id, video_id)
);

-- 9. TABLA: FAVORITOS
CREATE TABLE favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_video_favorite UNIQUE(user_id, video_id)
);

-- 10. TABLA: COMENTARIOS
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    content VARCHAR(1000) NOT NULL,
    is_flagged BOOLEAN DEFAULT false NOT NULL,
    created_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_comments_video_created ON comments(video_id, created_at DESC);

-- 11. TABLA: CONTROL DE SESIONES Y CONCURRENCIA DE STREAMING
CREATE TABLE active_session_streams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(100) NOT NULL,
    video_id UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    last_heartbeat_at TIMESTAMPTZ(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_session UNIQUE(user_id, session_id)
);

CREATE INDEX idx_active_streams_heartbeat ON active_session_streams(user_id, last_heartbeat_at);
