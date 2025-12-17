package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.conversation.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    
    /**
     * Find all messages for a conversation ordered by creation time
     * @param conversationId The conversation ID
     * @return List of chat messages
     */
    List<ChatMessage> findAllByConversationIdOrderByCreatedAtAsc(String conversationId);
}
