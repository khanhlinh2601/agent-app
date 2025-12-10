package org.linhtk.orchestrator.service;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.linhtk.common.exception.NotFoundException;
import org.linhtk.orchestrator.model.agent.Agent;
import org.linhtk.orchestrator.repository.AgentRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for dynamically creating and managing AI models based on agent configuration.
 * Uses built-in Spring AI observability through ObservationRegistry for comprehensive monitoring.
 */
@Service
@Slf4j
public class DynamicModelService {
    private final ToolCallingManager toolCallingManager;
    private final AgentRepository agentRepository;
    private final ObservationRegistry observationRegistry;
    
    // Single retry template for all OpenAI operations
    private final RetryTemplate retryTemplate;
    
    // Cache to store created models to avoid recreating them for the same agent
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();

    public DynamicModelService(ToolCallingManager toolCallingManager, 
                              AgentRepository agentRepository,
                              ObservationRegistry observationRegistry,
                              RetryTemplate retryTemplate) {
        this.toolCallingManager = toolCallingManager;
        this.agentRepository = agentRepository;
        this.observationRegistry = observationRegistry;
        this.retryTemplate = retryTemplate;
    }

    /**
     * Retrieves or creates chat and embedding models for the specified agent
     * @param agentId The agent identifier
     * @return Map containing ChatModel and EmbeddingModel pair
     * @throws NotFoundException if agent is not found
     */
    public Map<ChatModel, EmbeddingModel> getModels(String agentId) {
        log.debug("Getting models for agent: {}", agentId);
        Agent agent = findAgentById(agentId);
        
        ChatModel chatModel = getChatModel(agent);
        EmbeddingModel embeddingModel = getEmbeddingModel(agent);
        
        Map<ChatModel, EmbeddingModel> models = new HashMap<>();
        models.put(chatModel, embeddingModel);
        return models;
    }
    
    /**
     * Retrieves or creates an embedding model for the specified agent
     * @param agentId The agent identifier
     * @return EmbeddingModel configured for the agent
     * @throws NotFoundException if agent is not found
     */
    public EmbeddingModel getEmbeddingModel(String agentId) {
        Agent agent = findAgentById(agentId);
        return getEmbeddingModel(agent);
    }
    
    /**
     * Retrieves or creates a chat model for the specified agent
     * @param agentId The agent identifier
     * @return ChatModel configured for the agent
     * @throws NotFoundException if agent is not found
     */
    public ChatModel getChatModel(String agentId) {
        Agent agent = findAgentById(agentId);
        return getChatModel(agent);
    }

    /**
     * Creates or retrieves cached ChatModel for the agent
     * Implements efficient caching to avoid unnecessary model recreation
     */
    private ChatModel getChatModel(Agent agent) {
        boolean cacheHit = chatModelCache.containsKey(agent.getId());
        log.debug("Chat model cache {} for agent: {}", cacheHit ? "HIT" : "MISS", agent.getId());
        
        return chatModelCache.computeIfAbsent(agent.getId(), k -> {
            log.info("Creating new chat model for agent: {} with provider: {}", 
                    agent.getId(), agent.getProviderName());
            return createChatModel(agent);
        });
    }
    
    /**
     * Creates or retrieves cached EmbeddingModel for the agent
     * Implements efficient caching to avoid unnecessary model recreation
     */
    private EmbeddingModel getEmbeddingModel(Agent agent) {
        boolean cacheHit = embeddingModelCache.containsKey(agent.getId());
        log.debug("Embedding model cache {} for agent: {}", cacheHit ? "HIT" : "MISS", agent.getId());
        
        return embeddingModelCache.computeIfAbsent(agent.getId(), k -> {
            log.info("Creating new embedding model for agent: {} with provider: {}", 
                    agent.getId(), agent.getProviderName());
            return createEmbeddingModel(agent);
        });
    }
    
    /**
     * Creates a new ChatModel based on agent configuration
     * Currently supports OpenAI models with extensibility for other providers
     */
    private ChatModel createChatModel(Agent agent) {
        return switch (agent.getProviderName().toLowerCase()) {
            case "openai" -> createOpenAiChatModel(agent);
            default -> throw new IllegalArgumentException("Unsupported provider: " + agent.getProviderName());
        };
    }
    
    /**
     * Creates a new EmbeddingModel based on agent configuration  
     * Currently supports OpenAI models with extensibility for other providers
     */
    private EmbeddingModel createEmbeddingModel(Agent agent) {
        return switch (agent.getProviderName().toLowerCase()) {
            case "open ai" -> createOpenAiEmbeddingModel(agent);
            default -> throw new IllegalArgumentException("Unsupported provider: " + agent.getProviderName());
        };
    }
    
    /**
     * Creates OpenAI ChatModel with agent-specific configuration and tool calling support.
     * Uses built-in observability through OpenAI's native ObservationRegistry integration.
     */
    private ChatModel createOpenAiChatModel(Agent agent) {
        // Create OpenAI API instance with custom endpoint and API key if provided
        OpenAiApi openAiApi = createOpenAiApi(agent);
        
        // Configure chat options with the specified model name
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(agent.getProviderModelName())
                .temperature(agent.getTemperature())
                .maxTokens(agent.getMaxTokens())
                .topP(agent.getTopP())
                .build();
        // The OpenAI implementation handles metrics, tracing, and logging automatically
        ChatModel chatModel = new OpenAiChatModel(
            openAiApi,
            chatOptions,
            toolCallingManager,
            retryTemplate, // retryTemplate - using injected retry behavior
            observationRegistry
        );
        
        log.info("Successfully created OpenAI chat model for agent: {} with model: {}", 
                agent.getId(), agent.getProviderModelName());
        
        return chatModel;
    }
    
    /**
     * Creates OpenAI EmbeddingModel with agent-specific configuration.
     * Uses built-in observability through OpenAI's native ObservationRegistry integration.
     */
    private EmbeddingModel createOpenAiEmbeddingModel(Agent agent) {
        // Create OpenAI API instance with custom endpoint and API key if provided
        OpenAiApi openAiApi = createOpenAiApi(agent);
        
        // Use agent's configured embedding model name, fallback to text-embedding-ada-002 if not specified
        String embeddingModelName = agent.getProviderEmbeddingModelName();
        
        OpenAiEmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(embeddingModelName)
                .dimensions(agent.getDimension())
                .build();
        
        // Create OpenAI EmbeddingModel with built-in observability
        // The OpenAI implementation automatically tracks embedding operations
        EmbeddingModel embeddingModel =  new OpenAiEmbeddingModel(
            openAiApi,
            MetadataMode.ALL,
            embeddingOptions,
            retryTemplate, // retryTemplate - using injected retry behavior
            observationRegistry
        );
        
        log.info("Successfully created OpenAI embedding model for agent: {} with model: {}", 
                agent.getId(), embeddingModelName);
        
        return embeddingModel;
    }
    
    /**
     * Creates OpenAI API instance with custom configuration from agent
     * Handles both default OpenAI endpoints and custom endpoints for OpenAI-compatible services
     */
    private OpenAiApi createOpenAiApi(Agent agent) {
        // Use custom endpoint if provided, otherwise use default OpenAI endpoint
        String baseUrl = agent.getBaseUrl();

        log.debug("Creating OpenAI API instance with endpoint: {}", baseUrl);
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .completionsPath(agent.getChatCompletionsPath())
                .embeddingsPath(agent.getEmbeddingsPath())
                .apiKey(agent.getProviderApiKey())
                .build();
    }
    
    /**
     * Retrieves agent by ID with proper error handling
     * @param agentId The agent identifier to look up
     * @return Agent entity
     * @throws NotFoundException if agent is not found
     */
    private Agent findAgentById(String agentId) {
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (agentOpt.isEmpty()) {
            log.warn("Agent not found with ID: {}", agentId);
            throw new NotFoundException("Agent not found with ID: " + agentId);
        }
        
        Agent agent = agentOpt.get();
        log.debug("Found agent: {} with provider: {}", agent.getId(), agent.getProviderName());
        return agent;
    }
    
    /**
     * Clears the model cache for a specific agent
     * Useful when agent configuration changes and models need to be recreated
     * @param agentId The agent identifier whose cache should be cleared
     */
    public void clearAgentCache(String agentId) {
        boolean chatModelRemoved = chatModelCache.remove(agentId) != null;
        boolean embeddingModelRemoved = embeddingModelCache.remove(agentId) != null;
        
        log.info("Cache cleared for agent: {} (chat: {}, embedding: {})", 
                agentId, chatModelRemoved, embeddingModelRemoved);
    }
    
    /**
     * Clears all model caches
     * Useful for memory management or when configuration updates affect multiple agents
     */
    public void clearAllCaches() {
        int chatModelsCleared = chatModelCache.size();
        int embeddingModelsCleared = embeddingModelCache.size();
        
        chatModelCache.clear();
        embeddingModelCache.clear();
        
        log.info("All caches cleared - chat models: {}, embedding models: {}", 
                chatModelsCleared, embeddingModelsCleared);
    }
    
    /**
     * Gets cache statistics for monitoring and optimization
     * Provides insights into cache usage patterns and effectiveness
     * @return Map containing cache size statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("chatModelCacheSize", chatModelCache.size());
        stats.put("embeddingModelCacheSize", embeddingModelCache.size());
        stats.put("totalCacheSize", chatModelCache.size() + embeddingModelCache.size());
        
        log.debug("Cache statistics: {}", stats);
        return stats;
    }
}
