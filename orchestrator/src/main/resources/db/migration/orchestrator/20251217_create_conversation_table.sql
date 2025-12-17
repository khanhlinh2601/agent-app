-- Create conversation table
CREATE TABLE IF NOT EXISTS conversation (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    session_id VARCHAR(255),
    agent_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_conversation_agent FOREIGN KEY (agent_id) REFERENCES agent(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_conversation_agent_id ON conversation(agent_id);
CREATE INDEX idx_conversation_created_by ON conversation(created_by);
CREATE INDEX idx_conversation_created_at ON conversation(created_at DESC);
CREATE INDEX idx_conversation_agent_user ON conversation(agent_id, created_by, created_at DESC);
