package org.linhtk.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.linhtk.common.exception.BadRequestException;
import org.linhtk.common.exception.NotFoundException;
import org.linhtk.orchestrator.chunking.DocumentChunker;
import org.linhtk.orchestrator.model.knowledge.AgentKnowledge;
import org.linhtk.orchestrator.model.knowledge.KnowledgeChunk;
import org.linhtk.orchestrator.repository.AgentKnowledgeRepository;
import org.linhtk.orchestrator.dto.KnowledgeImportingResponseDto;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service for importing knowledge documents into the system.
 * Handles file validation, document chunking, embedding generation, and vector store persistence.
 * Integrates with Spring AI's document processing pipeline for comprehensive knowledge management.
 */
@Service
@Slf4j
public class KnowledgeImportService {
    private final KnowledgeChunkService chunkService;
    private final AgentKnowledgeRepository knowledgeRepository;
    private final DocumentChunker documentChunker;

    public KnowledgeImportService(KnowledgeChunkService chunkService, 
                                 AgentKnowledgeRepository knowledgeRepository, 
                                 DocumentChunker documentChunker) {
        this.chunkService = chunkService;
        this.knowledgeRepository = knowledgeRepository;
        this.documentChunker = documentChunker;
    }

    /**
     * Imports a document file and processes it into searchable knowledge chunks.
     * This method handles the complete pipeline from file upload to vector store indexing.
     * 
     * Implementation follows SOLID principles with clear separation of concerns:
     * 1. Input validation and security checks
     * 2. Knowledge ownership verification  
     * 3. Document processing and chunking
     * 4. Embedding generation and storage
     * 5. Comprehensive response creation
     * 
     * @param agentId The agent identifier that owns the knowledge
     * @param knowledgeId The knowledge source identifier to add chunks to
     * @param file The uploaded file to process
     * @return KnowledgeImportingResponseVm containing import results and statistics
     */
    @Transactional
    public KnowledgeImportingResponseDto importDocument(String agentId, String knowledgeId, MultipartFile file) {
        var fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        var contentType = file.getContentType();

        log.info("Starting document import: agent={}, knowledge={}, file={}, contentType={}, size={} bytes",
                agentId, knowledgeId, fileName, contentType, file.getSize());

        // Step 1: Validate input parameters following OWASP security practices
        validateImportRequest(agentId, knowledgeId, file);

        // Step 2: Validate knowledge ownership and existence
        AgentKnowledge knowledge = validateKnowledgeOwnership(agentId, knowledgeId);

        // Step 3: Validate file extension
        String fileExtension = extractFileExtension(fileName);
        if (fileExtension.isEmpty()) {
            throw new BadRequestException("File must have a valid extension");
        }

        try {
            // Step 4: Process document into chunks using Spring AI pipeline
            List<Document> documents = documentChunker.splitDocumentIntoChunks(file, null);

            if (documents.isEmpty()) {
                throw new BadRequestException("No processable content found in the document");
            }

            // Step 5: Get starting chunk order for proper sequencing
            int currentOrder = chunkService.getNextChunkOrderForKnowledge(agentId, knowledgeId);

            // Step 6: Process each chunk with embedding generation and storage
            String chunkingProfile = extractChunkingProfile(documents);
            
            for (int i = 0; i < documents.size(); i++) {
                Document document = documents.get(i);
                int chunkOrder = currentOrder + i;
                
                // Create chunk with embeddings and add to vector store
                KnowledgeChunk savedChunk = chunkService.addChunk(agentId, knowledgeId, document, chunkOrder);
                
                log.debug("Processed chunk {}/{}: id={}, order={}", 
                         i + 1, documents.size(), savedChunk.getId(), chunkOrder);
            }

            log.info("Successfully imported document: file={}, chunks={}, knowledge={}", 
                     fileName, documents.size(), knowledgeId);

            // Step 7: Build comprehensive response
            return KnowledgeImportingResponseDto.builder()
                    .originalFilename(fileName)
                    .numberOfSegments(documents.size())
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .chunkingProfile(chunkingProfile)
                    .knowledgeId(knowledgeId)
                    .agentId(agentId)
                    .build();

        } catch (Exception e) {
            log.error("Failed to import document: agent={}, knowledge={}, file={}, error={}", 
                      agentId, knowledgeId, fileName, e.getMessage(), e);
            throw new RuntimeException("Document import failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the import request parameters following OWASP security best practices.
     * Implements comprehensive input validation to prevent security vulnerabilities.
     */
    private void validateImportRequest(String agentId, String knowledgeId, MultipartFile file) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new BadRequestException("Agent ID cannot be null or empty");
        }
        
        if (knowledgeId == null || knowledgeId.trim().isEmpty()) {
            throw new BadRequestException("Knowledge ID cannot be null or empty");
        }
        
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be null or empty");
        }
        
        // File size validation - 10MB limit for security and performance
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }
        
        // Filename validation
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BadRequestException("File must have a valid name");
        }
        
        // Content type security check - prevent executable files
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("executable")) {
            throw new BadRequestException("Executable files are not allowed");
        }
    }

    /**
     * Validates knowledge ownership ensuring proper access control.
     * Implements the principle of least privilege by verifying agent ownership.
     */
    private AgentKnowledge validateKnowledgeOwnership(String agentId, String knowledgeId) {
        AgentKnowledge knowledge = knowledgeRepository.findById(knowledgeId)
            .orElseThrow(() -> new NotFoundException("Knowledge source not found with ID: " + knowledgeId));
        
        if (!agentId.equals(knowledge.getAgentId())) {
            throw new NotFoundException("Knowledge source " + knowledgeId + " does not belong to agent " + agentId);
        }
        
        log.debug("Validated knowledge ownership: knowledge={} belongs to agent={}", knowledgeId, agentId);
        return knowledge;
    }

    /**
     * Extracts file extension with proper validation and normalization.
     * Handles edge cases and ensures consistent processing.
     */
    private String extractFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Extracts chunking profile from processed documents for analytics and debugging.
     */
    private String extractChunkingProfile(List<Document> documents) {
        if (documents.isEmpty()) {
            return "unknown";
        }
        
        return documents.get(0).getMetadata()
                .getOrDefault("profile", "unknown")
                .toString();
    }
}
