package org.linhtk.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating and updating Agent entity.
 * Contains validation constraints for required fields.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentRequestDto {

    private String name;
    
    private String description;
    
    private String instructions;

    private String providerName;
    private String providerModelName;
    
    private String providerEmbeddingModelName;

    private String providerEndpoint;

    private String providerApiKey;
    
    private boolean isPublished;
    
    private boolean isDefault;
}
