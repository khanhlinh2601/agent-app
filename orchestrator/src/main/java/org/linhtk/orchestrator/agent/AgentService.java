package org.linhtk.orchestrator.agent;

import java.util.List;
import org.linhtk.orchestrator.repository.AgentRepository;

public class AgentService {
    private final AgentFactory agentFactory;
    private final AgentRepository agentRepository;

    private final int MAX_LATEST_MESSAGE_TO_KEEP_FULL = 5;

    public AgentService(AgentFactory agentFactory, AgentRepository agentRepository) {
        this.agentFactory = agentFactory;
        this.agentRepository = agentRepository;
    }




}
