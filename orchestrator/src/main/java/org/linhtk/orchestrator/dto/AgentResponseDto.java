package org.linhtk.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Response DTO for Agent entity.
 * Contains agent configuration and metadata for API responses.
 * Excludes sensitive information like API keys.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentResponseDto {
    private String id;
    private String name;
    private String description;
    private String instructions;
    private String providerName;
    private String providerModelName;
    private String providerEmbeddingModelName;
    private String baseUrl;
    private String providerEndpoint;
    private boolean isPublished;
    private boolean isDefault;
    private String createdBy;
    private ZonedDateTime createdAt;
    private String updatedBy;
    private ZonedDateTime updatedAt;
}
