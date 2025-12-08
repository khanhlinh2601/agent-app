package org.linhtk.orchestrator.model.agent;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.linhtk.common.model.AbstractAuditEntity;

/**
 * Entity representing AI Agent configuration.
 * Stores LLM provider settings, model parameters, and agent behavior instructions.
 */
@Entity
@Table(name = "agent")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Agent extends AbstractAuditEntity {
    
    /**
     * Unique identifier for the agent.
     * Generated as UUID for distributed system compatibility.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Human-readable description of the agent's purpose and capabilities.
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * System instructions that define agent behavior and personality.
     * Used as system prompt in chat completions.
     */
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    /**
     * LLM provider name (e.g., OPENAI, AZURE, GITHUB_MODELS).
     */
    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;
    
    /**
     * Model name for chat completions (e.g., gpt-4, gpt-3.5-turbo).
     */
    @Column(name = "provider_model_name", nullable = false, length = 50)
    private String providerModelName;
    
    /**
     * Model name for generating embeddings (e.g., text-embedding-ada-002).
     */
    @Column(name = "provider_embedding_model_name", nullable = false, length = 50)
    private String providerEmbeddingModelName;
    
    /**
     * Embedding vector dimension (768 or 1536).
     * Must match the embedding model's output dimension.
     */
    @Column(nullable = false)
    private Integer dimension;
    
    /**
     * Base URL for the LLM provider API.
     */
    @Column(name = "base_url", length = 255)
    private String baseUrl;
    
    /**
     * API path for embeddings endpoint.
     */
    @Column(name = "embeddings_path", nullable = false, length = 100)
    private String embeddingsPath;
    
    /**
     * API path for chat completions endpoint.
     */
    @Column(name = "chat_completions_path", nullable = false, length = 100)
    private String chatCompletionsPath;
    
    /**
     * API key for authenticating with the LLM provider.
     * Should be encrypted at rest in production environments.
     */
    @Column(name = "provider_api_key", nullable = false, length = 200)
    private String providerApiKey;
    
    /**
     * Flag indicating if this is the default agent for new conversations.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    /**
     * Temperature parameter controlling randomness in responses (0.0 to 2.0).
     * Higher values make output more random, lower values more deterministic.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double temperature = 0.7;
    
    /**
     * Maximum number of tokens to generate in responses.
     */
    @Column(name = "max_tokens", nullable = false)
    @Builder.Default
    private Integer maxTokens = 2048;
    
    /**
     * Top-p (nucleus sampling) parameter (0.0 to 1.0).
     * Controls diversity via nucleus sampling.
     */
    @Column(name = "top_p", nullable = false)
    @Builder.Default
    private Double topP = 1.0;
}
