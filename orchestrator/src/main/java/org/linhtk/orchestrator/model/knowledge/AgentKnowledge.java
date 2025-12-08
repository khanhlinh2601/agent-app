package org.linhtk.orchestrator.model.knowledge;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.linhtk.common.model.AbstractAuditEntity;
import org.linhtk.orchestrator.constant.KnowledgeSourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing agent knowledge sources.
 * Stores information about knowledge sources associated with AI agents,
 * including their type, URI, and metadata for retrieval and processing.
 */
@Entity
@Table(name = "agent_knowledge")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentKnowledge extends AbstractAuditEntity {
    
    /**
     * Unique identifier for the knowledge source.
     * Generated as UUID for distributed system compatibility.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Reference to the parent agent that owns this knowledge source.
     * Stored as UUID string without foreign key constraint for flexibility.
     */
    @Column(name = "agent_id", nullable = false, length = 50)
    private String agentId;

    /**
     * Human-readable name for the knowledge source.
     * Limited to 100 characters for display purposes.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of knowledge source (e.g., DOCUMENT, URL, DATABASE).
     * Stored as string enum for flexibility and readability.
     */
    @Column(name = "source_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private KnowledgeSourceType sourceType;

    /**
     * URI or path to the knowledge source.
     * Nullable to support knowledge sources that don't have external URIs.
     */
    @Column(name = "source_uri")
    private String sourceUri;

    /**
     * JSON metadata for storing flexible knowledge source configuration.
     * Uses PostgreSQL JSONB for efficient storage and querying.
     * Initialized to empty map to avoid null pointer exceptions.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
