package org.linhtk.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.linhtk.common.exception.NotFoundException;
import org.linhtk.orchestrator.model.knowledge.AgentKnowledge;
import org.linhtk.orchestrator.model.knowledge.KnowledgeChunk;
import org.linhtk.orchestrator.repository.AgentKnowledgeRepository;
import org.linhtk.orchestrator.repository.KnowledgeChunkRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for managing knowledge chunks.
 * Handles chunk creation, embedding generation, and storage operations.
 * Integrates with Spring AI embedding models for semantic search capabilities.
 */
@Service
@Slf4j
public class KnowledgeChunkService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final AgentKnowledgeRepository agentKnowledgeRepository;
    private final DynamicModelService dynamicModelService;
    private final VectorStoreService vectorStoreService;

    public KnowledgeChunkService(KnowledgeChunkRepository knowledgeChunkRepository,
                                 AgentKnowledgeRepository agentKnowledgeRepository,
                                 DynamicModelService dynamicModelService,
                                 VectorStoreService vectorStoreService) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.agentKnowledgeRepository = agentKnowledgeRepository;
        this.dynamicModelService = dynamicModelService;
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * Adds a new chunk with embeddings to the knowledge base.
     * Generates embeddings using the agent's configured embedding model
     * and stores both the chunk and vector representation.
     *
     * @param agentId     The agent identifier
     * @param knowledgeId The knowledge source identifier
     * @param document    The document chunk to process
     * @param chunkOrder  The sequential order of this chunk
     * @return The saved KnowledgeChunk entity
     */
    @Transactional
    public KnowledgeChunk addChunk(String agentId, String knowledgeId, Document document, int chunkOrder) {
        log.debug("Adding chunk for knowledge: {}, order: {}", knowledgeId, chunkOrder);

        // Validate ownership
        validateKnowledgeOwnership(agentId, knowledgeId);

        // Get embedding model for the agent
        EmbeddingModel embeddingModel = dynamicModelService.getEmbeddingModel(agentId);

        // Generate embeddings for the document content
        EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(document.getText()));
        float[] embedding = embeddingResponse.getResults().get(0).getOutput();

        // Determine embedding dimension and store accordingly
        int dimension = embedding.length;

        // Build knowledge chunk entity
        KnowledgeChunk chunk = KnowledgeChunk.builder()
                .knowledgeId(knowledgeId)
                .content(document.getText())
                .chunkOrder(chunkOrder)
                .metadata(document.getMetadata())
                .build();

        // Store embedding in appropriate field based on dimension
        if (dimension == 768) {
            chunk.setEmbedding768(embedding);
        } else if (dimension == 1536) {
            chunk.setEmbedding1536(embedding);
        } else {
            log.warn("Unsupported embedding dimension: {}. Chunk will be saved without embeddings.", dimension);
        }

        // Save chunk to database
        KnowledgeChunk savedChunk = knowledgeChunkRepository.save(chunk);

        // Add to vector store for semantic search
        try {
            var vectorStore = vectorStoreService.vectorStore(agentId);
            vectorStore.add(List.of(document));
            log.debug("Successfully added chunk {} to vector store", savedChunk.getId());
        } catch (Exception e) {
            log.error("Failed to add chunk to vector store: {}", e.getMessage(), e);
            // Continue execution - chunk is saved even if vector store addition fails
        }

        return savedChunk;
    }

    /**
     * Gets the next chunk order for a knowledge source.
     * Validates knowledge ownership before returning order.
     *
     * @param agentId     The agent identifier
     * @param knowledgeId The knowledge source identifier
     * @return The next available chunk order number
     */
    public int getNextChunkOrderForKnowledge(String agentId, String knowledgeId) {
        // Validate ownership first - ensures knowledge exists and belongs to agent
        validateKnowledgeOwnership(agentId, knowledgeId);

        // Query with both knowledgeId and agentId for defense-in-depth security
        int currentMaxOrder = knowledgeChunkRepository.findMaxChunkOrderByKnowledgeIdAndAgentId(knowledgeId, agentId);
        return currentMaxOrder + 1;
    }

    /**
     * Validates that the knowledge source belongs to the specified agent.
     * Implements access control to ensure agents can only access their own knowledge.
     *
     * @param agentId     The agent identifier to validate
     * @param knowledgeId The knowledge source identifier to validate
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to agent
     */
    private void validateKnowledgeOwnership(String agentId, String knowledgeId) {
        AgentKnowledge knowledge = agentKnowledgeRepository.findById(knowledgeId)
                .orElseThrow(() -> new NotFoundException("Knowledge source not found with ID: " + knowledgeId));

        if (!agentId.equals(knowledge.getAgentId())) {
            throw new NotFoundException("Knowledge source " + knowledgeId + " does not belong to agent " + agentId);
        }

        log.debug("Validated knowledge ownership: knowledge={} belongs to agent={}", knowledgeId, agentId);
    }

    /**
     * Retrieves all knowledge chunks for a specific knowledge source.
     * Returns chunks ordered by their chunk order to maintain document structure.
     *
     * @param agentId     The agent identifier
     * @param knowledgeId The knowledge source identifier
     * @return List of knowledge chunks ordered by chunk order
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to agent
     */
    public List<KnowledgeChunk> getByKnowledge(String agentId, String knowledgeId) {
        log.debug("Getting chunks for knowledge: {}, agent: {}", knowledgeId, agentId);

        // Validate ownership first - ensures knowledge exists and belongs to agent
        validateKnowledgeOwnership(agentId, knowledgeId);

        // Retrieve all chunks for this knowledge source
        List<KnowledgeChunk> chunks = knowledgeChunkRepository.findAllByKnowledgeIdAndAgentId(knowledgeId, agentId);

        log.debug("Found {} chunks for knowledge: {}", chunks.size(), knowledgeId);
        return chunks;
    }

    /**
     * Searches for similar chunks using semantic similarity search.
     * Uses the agent's vector store to find the most relevant chunks based on the query.
     *
     * @param agentId     The agent identifier
     * @param knowledgeId The knowledge source identifier to search within
     * @param query       The search query text
     * @param topK        The number of top results to return (default: 5)
     * @return List of similar knowledge chunks ordered by relevance
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to agent
     */
    public List<KnowledgeChunk> searchSimilarChunks(String agentId, String knowledgeId, String query, int topK) {
        log.debug("Searching similar chunks for knowledge: {}, agent: {}, query: {}, topK: {}",
                knowledgeId, agentId, query, topK);

        // Validate ownership first
        validateKnowledgeOwnership(agentId, knowledgeId);

        // Validate input parameters
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }

        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }

        try {
            // Get vector store for the agent
            var vectorStore = vectorStoreService.vectorStore(agentId);

            // Perform similarity search using Spring AI VectorStore
            // The search returns documents ordered by similarity score
            List<org.springframework.ai.document.Document> similarDocuments =
                    vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());

            log.debug("Vector store returned {} similar documents", similarDocuments.size());

            // Extract chunk IDs from the document metadata and retrieve full chunks
            List<KnowledgeChunk> similarChunks = new java.util.ArrayList<>();

            for (org.springframework.ai.document.Document doc : similarDocuments) {
                // The document ID should correspond to the chunk ID
                String chunkId = doc.getId();
                if (chunkId != null) {
                    knowledgeChunkRepository.findById(chunkId).ifPresent(chunk -> {
                        // Only include chunks from the specified knowledge source
                        if (knowledgeId.equals(chunk.getKnowledgeId()) && agentId.equals(chunk.getAgentId())) {
                            similarChunks.add(chunk);
                        }
                    });
                }
            }

            log.info("Found {} similar chunks for knowledge: {}, query: '{}'",
                    similarChunks.size(), knowledgeId, query);

            return similarChunks;

        } catch (Exception e) {
            log.error("Failed to search similar chunks: knowledge={}, error={}",
                    knowledgeId, e.getMessage(), e);
            throw new RuntimeException("Similarity search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing knowledge chunk with new content and metadata.
     * Regenerates embeddings for the new content and updates the vector store.
     *
     * @param agentId     The agent identifier
     * @param knowledgeId The knowledge source identifier
     * @param chunkId     The chunk identifier to update
     * @param newContent  The new content for the chunk
     * @param newMetadata The new metadata for the chunk (optional, can be empty)
     * @return The updated KnowledgeChunk entity
     * @throws NotFoundException if chunk, knowledge, or agent doesn't exist or access denied
     */
    @Transactional
    public KnowledgeChunk updateChunk(String agentId, String knowledgeId, String chunkId,
                                      String newContent, Map<String, Object> newMetadata) {
        log.debug("Updating chunk: id={}, knowledge={}, agent={}", chunkId, knowledgeId, agentId);

        // Validate ownership
        validateKnowledgeOwnership(agentId, knowledgeId);

        // Validate input parameters
        if (chunkId == null || chunkId.trim().isEmpty()) {
            throw new IllegalArgumentException("Chunk ID cannot be null or empty");
        }

        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("New content cannot be null or empty");
        }

        // Retrieve the existing chunk
        KnowledgeChunk existingChunk = knowledgeChunkRepository.findById(chunkId)
                .orElseThrow(() -> new NotFoundException("Chunk not found with ID: " + chunkId));

        // Verify the chunk belongs to the specified knowledge and agent
        if (!knowledgeId.equals(existingChunk.getKnowledgeId())) {
            throw new NotFoundException("Chunk " + chunkId + " does not belong to knowledge " + knowledgeId);
        }

        if (!agentId.equals(existingChunk.getAgentId())) {
            throw new NotFoundException("Chunk " + chunkId + " does not belong to agent " + agentId);
        }

        try {
            // Get embedding model for the agent
            EmbeddingModel embeddingModel = dynamicModelService.getEmbeddingModel(agentId);

            // Generate new embeddings for the updated content
            EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(newContent));
            float[] newEmbedding = embeddingResponse.getResults().get(0).getOutput();

            // Determine embedding dimension
            int dimension = newEmbedding.length;

            // Update chunk content
            existingChunk.setContent(newContent);

            // Update metadata if provided
            if (newMetadata != null && !newMetadata.isEmpty()) {
                existingChunk.setMetadata(newMetadata);
            }

            // Update embedding in appropriate field based on dimension
            if (dimension == 768) {
                existingChunk.setEmbedding768(newEmbedding);
                existingChunk.setEmbedding1536(null); // Clear other dimension
            } else if (dimension == 1536) {
                existingChunk.setEmbedding1536(newEmbedding);
                existingChunk.setEmbedding768(null); // Clear other dimension
            } else {
                log.warn("Unsupported embedding dimension: {}. Chunk will be saved without embeddings.", dimension);
                existingChunk.setEmbedding768(null);
                existingChunk.setEmbedding1536(null);
            }

            // Save updated chunk to database
            KnowledgeChunk updatedChunk = knowledgeChunkRepository.save(existingChunk);

            // Update vector store with new embeddings
            try {
                var vectorStore = vectorStoreService.vectorStore(agentId);

                // Create document with updated content for vector store
                Document updatedDocument =
                        Document.builder()
                                .id(chunkId)
                                .text(newContent)
                                .metadata(newMetadata != null ? newMetadata : existingChunk.getMetadata())
                                .build();

                // Delete old vector and add new one
                vectorStore.delete(List.of(chunkId));
                vectorStore.add(List.of(updatedDocument));

                log.debug("Successfully updated chunk {} in vector store", chunkId);
            } catch (Exception e) {
                log.error("Failed to update chunk in vector store: {}", e.getMessage(), e);
                // Continue execution - chunk is updated in DB even if vector store update fails
            }

            log.info("Successfully updated chunk: id={}, knowledge={}", chunkId, knowledgeId);
            return updatedChunk;

        } catch (Exception e) {
            log.error("Failed to update chunk: id={}, error={}", chunkId, e.getMessage(), e);
            throw new RuntimeException("Chunk update failed: " + e.getMessage(), e);
        }
    }
}
















