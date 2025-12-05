package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.knowledge.AgentKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentKnowledgeRepository extends JpaRepository<AgentKnowledge, String> {

}

