package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.knowledge.AgentKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AgentKnowledge entity.
 * Provides data access methods for agent knowledge sources.
 */
public interface AgentKnowledgeRepository extends JpaRepository<AgentKnowledge, String> {
    
    /**
     * Finds all knowledge sources associated with a specific agent.
     * 
     * @param agentId The unique identifier of the agent
     * @return List of all knowledge sources for the agent
     */
    List<AgentKnowledge> findByAgentId(String agentId);
    
    /**
     * Finds a specific knowledge source for an agent by knowledge ID.
     * Used to verify knowledge ownership before updates/deletes.
     * 
     * @param agentId The unique identifier of the agent
     * @param id The unique identifier of the knowledge source
     * @return Optional containing the knowledge if found and belongs to the agent
     */
    Optional<AgentKnowledge> findByAgentIdAndId(String agentId, String id);
}

