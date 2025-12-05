package org.linhtk.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response view model for knowledge import operations.
 * Contains information about the imported file and processing results.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KnowledgeImportingResponseDto {
    
    /**
     * Original filename of the imported file
     */
    private String originalFilename;
    
    /**
     * Number of chunks/segments created from the document
     */
    private int numberOfSegments;
    
    /**
     * Content type of the imported file
     */
    private String contentType;
    
    /**
     * Size of the original file in bytes
     */
    private long fileSize;
    
    /**
     * Chunking profile used for processing the document
     */
    private String chunkingProfile;
    
    /**
     * Knowledge source ID where chunks were stored
     */
    private String knowledgeId;
    
    /**
     * Agent ID that owns the knowledge
     */
    private String agentId;
}
