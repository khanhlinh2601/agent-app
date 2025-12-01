package org.linhtk.orchestrator.repository;

import java.util.List;
import org.linhtk.orchestrator.model.agent.AgentTools;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentToolsRepository extends JpaRepository<AgentTools, String> {
    List<AgentTools> findAllByAgentId(String agentId);
}
