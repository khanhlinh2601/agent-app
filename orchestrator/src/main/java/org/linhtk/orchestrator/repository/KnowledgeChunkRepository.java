package org.linhtk.orchestrator.repository;

import org.linhtk.orchestrator.model.knowledge.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for KnowledgeChunk entity.
 * Provides data access operations for knowledge chunks including custom queries
 * for chunk ordering and retrieval.
 */
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, String> {
    
    /**
     * Finds the maximum chunk order for a specific knowledge source and agent.
     * Returns 0 if no chunks exist for the knowledge source, allowing
     * the first chunk to start at order 1.
     * 
     * This method enforces agent ownership validation at the query level,
     * ensuring that agents can only access chunk ordering information
     * for knowledge sources they own. This follows OWASP security best practices
     * by implementing access control at the data access layer.
     * 
     * Uses JPA entity relationship navigation (c.knowledge.agent.id) for clean,
     * type-safe queries that leverage Hibernate's entity graph traversal.
     * PostgreSQL will generate efficient JOIN queries from this JPQL.
     * 
     * @param knowledgeId The knowledge source identifier
     * @param agentId The agent identifier to validate ownership
     * @return The maximum chunk order, or 0 if no chunks exist
     */
    @Query("""
        SELECT COALESCE(MAX(c.chunkOrder), 0)
        FROM KnowledgeChunk c
        WHERE c.agentKnowledgeId = :knowledgeId AND c.agentId  = :agentId
        """)
    int findMaxChunkOrderByKnowledgeIdAndAgentId(@Param("knowledgeId") String knowledgeId, 
                                                   @Param("agentId") String agentId);
    
    /**
     * Finds all knowledge chunks for a specific knowledge source and agent.
     * Returns chunks ordered by their chunk order for maintaining document structure.
     * 
     * @param knowledgeId The knowledge source identifier
     * @param agentId The agent identifier to validate ownership
     * @return List of knowledge chunks ordered by chunkOrder
     */
    @Query("""
        SELECT c
        FROM KnowledgeChunk c
        WHERE c.agentKnowledgeId = :knowledgeId AND c.agentId = :agentId
        ORDER BY c.chunkOrder ASC
        """)
    List<KnowledgeChunk> findAllByKnowledgeIdAndAgentId(@Param("knowledgeId") String knowledgeId,
                                                        @Param("agentId") String agentId);
}
