package org.linhtk.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

/**
 * Request DTO for importing file-based knowledge.
 * Used when creating knowledge from file uploads.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FileKnowledgeImportConfigRequestDto {
    @NotBlank(message = "Knowledge name is required")
    private String name;
    
    private Map<String, Object> metadata;
}
