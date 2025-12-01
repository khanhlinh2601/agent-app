package org.linhtk.orchestrator.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

public interface AgentFactory {
    ChatClient createAgent(String agentId);
    ChatClient createBasicAgent(String instructions);
}