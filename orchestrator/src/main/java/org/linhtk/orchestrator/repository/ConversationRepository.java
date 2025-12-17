package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    /**
     * Find all conversations for a specific agent and user
     * @param agentId The agent ID
     * @param createdBy The user ID (from audit field)
     * @return List of conversations
     */
    List<Conversation> findAllByAgentIdAndCreatedByOrderByCreatedAtDesc(String agentId, String createdBy);
}
