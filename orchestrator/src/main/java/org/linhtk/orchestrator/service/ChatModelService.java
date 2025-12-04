package org.linhtk.orchestrator.service;


import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.repository.AgentToolsRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatModelService {
    private DynamicModelService dynamicModelService;
    private VectorStoreService vectorStoreService;
    private AgentToolsRepository  agentToolsRepository;


    public ChatModelService(DynamicModelService dynamicModelService, VectorStoreService vectorStoreService) {
        this.dynamicModelService = dynamicModelService;
        this.vectorStoreService = vectorStoreService;
    }

    //Chat streaming pipeline
    //1. Validate: Ensures conversation exists and user has access (authenticated or session-based) [Priority: Low]
    //2. History: Loads message history with automatic summarization [Priority: High]
//    If history ≤ 5 messages: return all messages
//    If history > 5 messages:
//    Summarize all but the latest 5 messages
//    Store/update summary in a message with IsSummary=true

    //3. Invoke: Creates agent and streams response [Priority: High]
    //4. Save: Persists user prompt and assistant response [Priority: High]


    //Create Tool for agent
    //Create knowledge for agent









}
