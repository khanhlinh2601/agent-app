package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.agent.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, String> {
}
