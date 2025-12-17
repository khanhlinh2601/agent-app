-- Create chat_message table
CREATE TABLE IF NOT EXISTS chat_message (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    agent_id VARCHAR(36) NOT NULL,
    type INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_agent FOREIGN KEY (agent_id) REFERENCES agent(id) ON DELETE CASCADE,
    CONSTRAINT chk_message_type CHECK (type IN (0, 1, 2))
);

-- Create indexes for performance
CREATE INDEX idx_chat_message_conversation_id ON chat_message(conversation_id);
CREATE INDEX idx_chat_message_agent_id ON chat_message(agent_id);
CREATE INDEX idx_chat_message_created_at ON chat_message(created_at ASC);
CREATE INDEX idx_chat_message_conversation_created ON chat_message(conversation_id, created_at ASC);

-- Add comment for type field
COMMENT ON COLUMN chat_message.type IS '0=USER, 1=ASSISTANT, 2=SYSTEM';
