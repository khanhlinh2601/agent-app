-- ====================================================================
-- Purpose: Create schema for AI Agent Management System
-- Database: PostgreSQL 13+
-- Features: UUID primary keys, no foreign keys, strategic indexes
-- ====================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "vector";

-- ====================================================================
-- TABLE: users
-- Purpose: Store system user accounts for authentication and audit tracking
-- ====================================================================
CREATE TABLE IF NOT EXISTS users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(50) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    name                VARCHAR(100) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(50)
);

-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled) WHERE deleted = FALSE;

COMMENT ON TABLE users IS 'System users for authentication and audit tracking';
COMMENT ON COLUMN users.id IS 'Unique identifier for the user';
COMMENT ON COLUMN users.username IS 'Unique username for authentication';
COMMENT ON COLUMN users.password IS 'Encrypted password (BCrypt)';
COMMENT ON COLUMN users.enabled IS 'Flag indicating if account is active';

-- ====================================================================
-- TABLE: agent
-- Purpose: Store AI agent configurations and LLM provider settings
-- ====================================================================
CREATE TABLE IF NOT EXISTS agent (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description                     TEXT,
    instructions                    TEXT,
    provider_name                   VARCHAR(50) NOT NULL,
    provider_model_name             VARCHAR(50) NOT NULL,
    provider_embedding_model_name   VARCHAR(50) NOT NULL,
    dimension                       INTEGER NOT NULL CHECK (dimension IN (768, 1536)),
    base_url                        VARCHAR(255),
    embeddings_path                 VARCHAR(100) NOT NULL,
    chat_completions_path           VARCHAR(100) NOT NULL,
    provider_api_key                VARCHAR(200) NOT NULL,
    is_default                      BOOLEAN NOT NULL DEFAULT FALSE,
    top_p                           NUMERIC(3,2) NOT NULL DEFAULT 1.0 CHECK (top_p >= 0 AND top_p <= 1),
    temperature                     NUMERIC(3,2) NOT NULL DEFAULT 0.7 CHECK (temperature >= 0 AND temperature <= 2),
    max_tokens                      INTEGER NOT NULL DEFAULT 2048 CHECK (max_tokens > 0),
    deleted                         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                      VARCHAR(50),
    updated_at                      TIMESTAMPTZ,
    updated_by                      VARCHAR(50)
);

-- Indexes for agent table - optimized for common queries
CREATE INDEX IF NOT EXISTS idx_agent_provider_name ON agent(provider_name) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_provider_model ON agent(provider_model_name) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_is_default ON agent(is_default) WHERE deleted = FALSE AND is_default = TRUE;
CREATE INDEX IF NOT EXISTS idx_agent_created_at ON agent(created_at DESC) WHERE deleted = FALSE;

COMMENT ON TABLE agent IS 'AI agent configurations with LLM provider settings';
COMMENT ON COLUMN agent.provider_name IS 'LLM provider (OPENAI, AZURE, GITHUB_MODELS)';
COMMENT ON COLUMN agent.dimension IS 'Embedding vector dimension (768 or 1536)';
COMMENT ON COLUMN agent.is_default IS 'Flag for default agent selection';

-- ====================================================================
-- TABLE: agent_tools
-- Purpose: Store tools available to agents for extended capabilities
-- ====================================================================
CREATE TABLE IF NOT EXISTS agent_tools (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id            VARCHAR(50) NOT NULL,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    is_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    agent_tool_type     VARCHAR(50) NOT NULL,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(50)
);

-- Indexes for agent_tools table
CREATE INDEX IF NOT EXISTS idx_agent_tools_agent_id ON agent_tools(agent_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_tools_type ON agent_tools(agent_tool_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_tools_enabled ON agent_tools(agent_id, is_enabled) WHERE deleted = FALSE AND is_enabled = TRUE;
CREATE INDEX IF NOT EXISTS idx_agent_tools_name ON agent_tools(name) WHERE deleted = FALSE;

COMMENT ON TABLE agent_tools IS 'Tools that extend agent capabilities';
COMMENT ON COLUMN agent_tools.agent_id IS 'Reference to parent agent (no FK)';
COMMENT ON COLUMN agent_tools.agent_tool_type IS 'Tool category (WEB_SEARCH, DATABASE, API_CALL)';

-- ====================================================================
-- TABLE: agent_knowledge
-- Purpose: Store knowledge sources associated with agents
-- ====================================================================
CREATE TABLE IF NOT EXISTS agent_knowledge (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id            VARCHAR(50) NOT NULL,
    name                VARCHAR(100) NOT NULL,
    source_type         VARCHAR(50) NOT NULL,
    source_uri          TEXT,
    metadata            JSONB NOT NULL DEFAULT '{}'::jsonb,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50),
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(50)
);

-- Indexes for agent_knowledge table
CREATE INDEX IF NOT EXISTS idx_agent_knowledge_agent_id ON agent_knowledge(agent_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_knowledge_source_type ON agent_knowledge(source_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_knowledge_name ON agent_knowledge(name) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_agent_knowledge_metadata ON agent_knowledge USING gin(metadata);

COMMENT ON TABLE agent_knowledge IS 'Knowledge sources for agent context';
COMMENT ON COLUMN agent_knowledge.agent_id IS 'Reference to parent agent (no FK)';
COMMENT ON COLUMN agent_knowledge.source_type IS 'Type of knowledge source (DOCUMENT, URL, DATABASE)';
COMMENT ON COLUMN agent_knowledge.metadata IS 'JSONB metadata for flexible configuration';

-- ====================================================================
-- TABLE: knowledge_chunk
-- Purpose: Store chunked content with vector embeddings for semantic search
-- ====================================================================
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_knowledge_id      VARCHAR(50) NOT NULL,
    agent_id                VARCHAR(50) NOT NULL,
    chunk_order             INTEGER NOT NULL DEFAULT 0,
    content                 TEXT NOT NULL,
    metadata                JSONB NOT NULL DEFAULT '{}'::jsonb,
    embedding_768           vector(768),
    embedding_1536          vector(1536),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(50),
    updated_at              TIMESTAMPTZ,
    updated_by              VARCHAR(50),
    CONSTRAINT uq_knowledge_chunk_order UNIQUE (agent_knowledge_id, chunk_order)
);

-- Indexes for knowledge_chunk table - optimized for semantic search
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_agent_knowledge_id ON knowledge_chunk(agent_knowledge_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_agent_id ON knowledge_chunk(agent_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_order ON knowledge_chunk(agent_knowledge_id, chunk_order);

-- Vector similarity search indexes using HNSW algorithm for fast approximate nearest neighbor search
-- HNSW (Hierarchical Navigable Small World) provides excellent performance for high-dimensional vectors
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_embedding_768_hnsw 
    ON knowledge_chunk USING hnsw (embedding_768 vector_cosine_ops)
    WHERE embedding_768 IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_embedding_1536_hnsw 
    ON knowledge_chunk USING hnsw (embedding_1536 vector_cosine_ops)
    WHERE embedding_1536 IS NOT NULL;

-- GIN index for metadata queries
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_metadata ON knowledge_chunk USING gin(metadata);

COMMENT ON TABLE knowledge_chunk IS 'Chunked content with vector embeddings for RAG';
COMMENT ON COLUMN knowledge_chunk.agent_knowledge_id IS 'Reference to parent knowledge source (no FK)';
COMMENT ON COLUMN knowledge_chunk.agent_id IS 'Denormalized agent reference for faster queries';
COMMENT ON COLUMN knowledge_chunk.chunk_order IS 'Sequential order within parent knowledge';
COMMENT ON COLUMN knowledge_chunk.embedding_768 IS '768-dim vector for semantic search (smaller models)';
COMMENT ON COLUMN knowledge_chunk.embedding_1536 IS '1536-dim vector for semantic search (OpenAI models)';

-- ====================================================================
-- END OF MIGRATION
-- ====================================================================