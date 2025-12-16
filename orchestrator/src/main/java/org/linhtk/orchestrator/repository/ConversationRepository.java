package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
}
