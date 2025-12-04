package org.linhtk.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * Service for creating PgVectorStore instances for agents.
 * Uses dynamic embedding models and dimension-based table naming.
 */
@Service
@Slf4j
public class VectorStoreService {
    
    private final DynamicModelService dynamicModelService;
    private final JdbcTemplate jdbcTemplate;
    
    public VectorStoreService(DynamicModelService dynamicModelService, JdbcTemplate jdbcTemplate) {
        this.dynamicModelService = dynamicModelService;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Creates and returns a PgVectorStore instance for the specified agent.
     * Uses the agent's embedding model to determine vector dimensions and table configuration.
     * 
     * @param agentId The unique identifier for the agent
     * @return PgVectorStore configured for the agent's embedding model
     */
    public VectorStore vectorStore(String agentId) {
        var embeddingModel = dynamicModelService.getEmbeddingModel(agentId);
        var dimensions = embeddingModel.dimensions();

        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .build();
    }
}