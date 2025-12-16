package org.linhtk.orchestrator.service;


import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.constant.ChatConfig;
import org.linhtk.orchestrator.dto.ChatRequestDto;
import org.linhtk.orchestrator.model.agent.AgentTools;
import org.linhtk.orchestrator.repository.AgentKnowledgeRepository;
import org.linhtk.orchestrator.repository.AgentToolsRepository;
import org.linhtk.orchestrator.service.tool.ToolRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ChatModelService {
    private final DynamicModelService dynamicModelService;
    private final VectorStoreService vectorStoreService;
    private final AgentToolsRepository agentToolsRepository;
    private final AgentKnowledgeRepository agentKnowledgeRepository;
    private final ToolRegistry toolRegistry;


    public ChatModelService(DynamicModelService dynamicModelService,
                            VectorStoreService vectorStoreService,
                            AgentToolsRepository agentToolsRepository,
                            AgentKnowledgeRepository agentKnowledgeRepository,
                            ToolRegistry toolRegistry) {
        this.dynamicModelService = dynamicModelService;
        this.vectorStoreService = vectorStoreService;
        this.agentToolsRepository = agentToolsRepository;
        this.agentKnowledgeRepository = agentKnowledgeRepository;
        this.toolRegistry = toolRegistry;
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

    public Flux<String> call(ChatRequestDto requestDto, List<String> history, String summary) {
        var model = dynamicModelService.getChatModel(requestDto.getAgentId());
        var chatClient = ChatClient.builder(model).build();

        var ragAdvisor = createRAGAdvisorForAgent(requestDto.getAgentId());

        var promptBuilder = chatClient.prompt();

        if (ragAdvisor != null) {
            promptBuilder = promptBuilder.advisors(ragAdvisor);
        }

        // Build combined prompt with summary, user question, and chat history
        StringBuilder combinedPromptBuilder = new StringBuilder();

        if (summary != null && !summary.isBlank()) {
            combinedPromptBuilder.append("Conversation summary so far:\n");
            combinedPromptBuilder.append(summary).append("\n");
            combinedPromptBuilder.append("\n");
        }

        combinedPromptBuilder.append("User question: ").append(requestDto.getQuestion()).append("\n");

        if (history != null && !history.isEmpty()) {
            combinedPromptBuilder.append("Chat history:\n");
            history.forEach(item -> combinedPromptBuilder.append(item).append("\n"));
            combinedPromptBuilder.append("\n");
        }

        String combinedPrompt = combinedPromptBuilder.toString();

        return promptBuilder
                .system(ChatConfig.SYSTEM_PROMPT + ChatConfig.SEARCH_TOOL_INSTRUCTION)
                .toolCallbacks(createToolCallbackForAgent(requestDto.getAgentId()))
                .user(combinedPrompt)
                .stream()
                .content();
    }

    /**
     * Creates a RAG (Retrieval Augmented Generation) advisor for an agent.
     * Uses the agent's knowledge base for document retrieval.
     *
     * @param agentId The ID of the agent
     * @return RetrievalAugmentationAdvisor configured with the agent's vector store
     */
    public RetrievalAugmentationAdvisor createRAGAdvisorForAgent(String agentId) {
        log.debug("Creating RAG advisor for agent: {}", agentId);

        var documentRetriever = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(vectorStoreService.vectorStore(agentId))
                .topK(ChatConfig.TOP_K)
                .build();

        var queryAugmenter = ContextualQueryAugmenter
                .builder()
                .allowEmptyContext(true)
                .build();

        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .build();
    }

    /**
     * Creates tool callbacks for an agent based on their configured tools.
     * Only returns callbacks for tools that are enabled and registered in the ToolRegistry.
     *
     * @param agentId The ID of the agent
     * @return List of ToolCallback instances for the agent's enabled tools
     */
    public List<ToolCallback> createToolCallbackForAgent(String agentId) {
        log.debug("Creating tool callbacks for agent: {}", agentId);

        // Get all enabled tools for this agent
        List<AgentTools> agentTools = agentToolsRepository.findAllByAgentId(agentId)
                .stream()
                .filter(AgentTools::getIsEnabled)
                .toList();

        if (agentTools.isEmpty()) {
            log.debug("No enabled tools found for agent: {}", agentId);
            return Collections.emptyList();
        }

        List<ToolCallback> toolCallbacks = new ArrayList<>();

        agentTools.stream()
                .map(AgentTools::getName)
                .forEach(toolName -> toolRegistry.getTool(toolName).ifPresentOrElse(
                        function -> {
                            // Cast Function to ToolCallback - Spring AI accepts Function<?> as ToolCallback
                            ToolCallback toolCallback = (ToolCallback) function;
                            toolCallbacks.add(toolCallback);
                            log.debug("Added tool callback for: {}", toolName);
                        },
                        () -> log.warn("Tool '{}' is enabled for agent '{}' but not found in registry", toolName, agentId)
                ));

        log.info("Created {} tool callbacks for agent: {}", toolCallbacks.size(), agentId);
        return toolCallbacks;
    }
}
