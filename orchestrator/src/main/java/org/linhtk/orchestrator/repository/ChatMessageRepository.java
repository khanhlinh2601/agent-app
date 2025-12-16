package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.conversation.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
}
