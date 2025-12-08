package org.linhtk.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.linhtk.orchestrator.constant.KnowledgeSourceType;

import java.util.Map;

/**
 * Request DTO for creating or updating AgentKnowledge.
 * Contains all configurable properties of a knowledge source.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentKnowledgeRequestDto {
    @NotBlank(message = "Knowledge name is required")
    private String name;
    
    @NotNull(message = "Source type is required")
    private KnowledgeSourceType sourceType;
    
    private String sourceUri;
    
    private Map<String, Object> metadata;
}
