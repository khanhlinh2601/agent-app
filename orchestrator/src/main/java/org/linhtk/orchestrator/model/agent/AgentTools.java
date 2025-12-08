package org.linhtk.orchestrator.model.agent;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.linhtk.common.model.AbstractAuditEntity;

/**
 * Entity representing tools available to AI agents.
 * Tools extend agent capabilities by enabling specific actions like web search,
 * database queries, API calls, etc.
 */
@Entity
@Table(name = "agent_tools")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentTools extends AbstractAuditEntity {
    
    /**
     * Unique identifier for the agent tool.
     * Generated as UUID for distributed system compatibility.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Reference to the parent agent that owns this tool.
     * Stored as UUID string without foreign key constraint for flexibility.
     */
    @Column(name = "agent_id", nullable = false, length = 50)
    private String agentId;
    
    /**
     * Tool name for identification and function calling.
     * Should match the function name in the LLM tool schema.
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Description of what the tool does.
     * Used by the LLM to understand when to invoke this tool.
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Flag indicating if the tool is currently active.
     * Allows temporary disabling without deletion.
     */
    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;
    
    /**
     * Type/category of the tool (e.g., WEB_SEARCH, DATABASE, API_CALL).
     * Used for grouping and filtering tools.
     */
    @Column(name = "agent_tool_type", nullable = false, length = 50)
    private String agentToolType;
}
