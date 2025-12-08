package org.linhtk.orchestrator.dto;

import lombok.*;
import org.linhtk.orchestrator.constant.KnowledgeSourceType;

import java.util.Map;

/**
 * Response DTO for AgentKnowledge entity.
 * Contains all relevant information about a knowledge source associated with an agent.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentKnowledgeResponseDto {
    private String id;
    private String agentId;
    private String name;
    private KnowledgeSourceType sourceType;
    private String sourceUri;
    private Map<String, Object> metadata;
}
