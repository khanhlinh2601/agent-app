package org.linhtk.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.dto.AgentKnowledgeRequestDto;
import org.linhtk.orchestrator.dto.AgentKnowledgeResponseDto;
import org.linhtk.orchestrator.dto.FileKnowledgeImportConfigRequestDto;
import org.linhtk.orchestrator.service.AgentKnowledgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing agent knowledge sources.
 * Provides endpoints for CRUD operations on knowledge sources associated with agents.
 * 
 * Endpoints:
 * - GET /api/agents/{agentId}/knowledge - Retrieve all knowledge for an agent
 * - GET /api/agents/{agentId}/knowledge/{knowledgeId} - Retrieve specific knowledge
 * - POST /api/agents/{agentId}/knowledge - Create new knowledge source
 * - POST /api/agents/{agentId}/knowledge/file - Create knowledge from file import
 * - PUT /api/agents/{agentId}/knowledge/{knowledgeId} - Update existing knowledge
 * - DELETE /api/agents/{agentId}/knowledge/{knowledgeId} - Delete knowledge source
 */
@RestController
@RequestMapping("/api/agents/{agentId}/knowledge")
@Slf4j
@Tag(name = "Agent Knowledge Management", description = "APIs for managing agent knowledge sources")
public class AgentKnowledgeController {

    private final AgentKnowledgeService agentKnowledgeService;

    public AgentKnowledgeController(AgentKnowledgeService agentKnowledgeService) {
        this.agentKnowledgeService = agentKnowledgeService;
    }

    /**
     * Retrieves all knowledge sources for a specific agent.
     * Returns comprehensive list without pagination.
     * 
     * @param agentId The unique identifier of the agent
     * @return ResponseEntity containing list of knowledge sources
     */
    @GetMapping
    @Operation(summary = "Get all knowledge for agent", 
               description = "Retrieves all knowledge sources associated with a specific agent")
    public ResponseEntity<List<AgentKnowledgeResponseDto>> getAllForAgent(@PathVariable String agentId) {
        log.info("REST request to get all knowledge for agent: {}", agentId);
        
        List<AgentKnowledgeResponseDto> knowledgeSources = agentKnowledgeService.getByAgent(agentId);
        
        return ResponseEntity.ok(knowledgeSources);
    }

    /**
     * Retrieves a specific knowledge source for an agent.
     * Validates that knowledge belongs to the specified agent.
     * 
     * @param agentId The unique identifier of the agent
     * @param knowledgeId The unique identifier of the knowledge source
     * @return ResponseEntity containing knowledge details
     */
    @GetMapping("/{knowledgeId}")
    @Operation(summary = "Get knowledge by ID", 
               description = "Retrieves a specific knowledge source for an agent")
    public ResponseEntity<AgentKnowledgeResponseDto> getOneForAgent(
            @PathVariable String agentId,
            @PathVariable String knowledgeId) {
        log.info("REST request to get knowledge: {} for agent: {}", knowledgeId, agentId);
        
        AgentKnowledgeResponseDto knowledge = agentKnowledgeService.getOneForAgent(agentId, knowledgeId);
        
        return ResponseEntity.ok(knowledge);
    }

    /**
     * Creates a new knowledge source with full configuration.
     * Used for general knowledge source types (URL, DATABASE, etc.).
     * Validates request body and creates knowledge with all specified properties.
     * 
     * @param agentId The unique identifier of the agent
     * @param requestDto The knowledge creation request with full configuration
     * @return ResponseEntity containing created knowledge with HTTP 201 status
     */
    @PostMapping
    @Operation(summary = "Create new knowledge source", 
               description = "Creates a new knowledge source with specified configuration")
    public ResponseEntity<AgentKnowledgeResponseDto> create(
            @PathVariable String agentId,
            @Valid @RequestBody AgentKnowledgeRequestDto requestDto) {
        log.info("REST request to create knowledge for agent: {}, name: {}, type: {}", 
                agentId, requestDto.getName(), requestDto.getSourceType());
        
        AgentKnowledgeResponseDto createdKnowledge = agentKnowledgeService.create(agentId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdKnowledge);
    }

    /**
     * Creates a new knowledge source from file import.
     * Used specifically for file-based knowledge sources.
     * SourceType is automatically set to FILE.
     * 
     * Design decision:
     * - Separate endpoint for file imports to maintain clear API semantics
     * - Uses FileKnowledgeImportConfigRequestDto for file-specific configuration
     * - SourceUri will be set after file upload/processing in file service
     * 
     * @param agentId The unique identifier of the agent
     * @param requestDto The file knowledge import configuration
     * @return ResponseEntity containing created knowledge with HTTP 201 status
     */
    @PostMapping("/file")
    @Operation(summary = "Create knowledge from file import", 
               description = "Creates a new knowledge source from file import configuration")
    public ResponseEntity<AgentKnowledgeResponseDto> createFromFile(
            @PathVariable String agentId,
            @Valid @RequestBody FileKnowledgeImportConfigRequestDto requestDto) {
        log.info("REST request to create file knowledge for agent: {}, name: {}", 
                agentId, requestDto.getName());
        
        AgentKnowledgeResponseDto createdKnowledge = agentKnowledgeService.create(agentId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdKnowledge);
    }

    /**
     * Updates an existing knowledge source configuration.
     * Performs partial update - only fields present in request body are updated.
     * Validates knowledge ownership before update.
     * 
     * @param agentId The unique identifier of the agent
     * @param knowledgeId The unique identifier of the knowledge source to update
     * @param requestDto The knowledge update request with new values
     * @return ResponseEntity containing updated knowledge details
     */
    @PutMapping("/{knowledgeId}")
    @Operation(summary = "Update knowledge source", 
               description = "Updates an existing knowledge source configuration")
    public ResponseEntity<AgentKnowledgeResponseDto> update(
            @PathVariable String agentId,
            @PathVariable String knowledgeId,
            @Valid @RequestBody AgentKnowledgeRequestDto requestDto) {
        log.info("REST request to update knowledge: {} for agent: {}", knowledgeId, agentId);
        
        AgentKnowledgeResponseDto updatedKnowledge = agentKnowledgeService.update(agentId, knowledgeId, requestDto);
        
        return ResponseEntity.ok(updatedKnowledge);
    }

    /**
     * Deletes a knowledge source from the system.
     * Performs hard delete as soft delete is not required.
     * Validates knowledge ownership before deletion.
     * 
     * Implementation notes:
     * - Returns 204 No Content on successful deletion
     * - Validates that knowledge belongs to agent before deletion
     * - Consider cleanup of associated files/embeddings in service layer
     * 
     * @param agentId The unique identifier of the agent
     * @param knowledgeId The unique identifier of the knowledge source to delete
     * @return ResponseEntity with HTTP 204 status
     */
    @DeleteMapping("/{knowledgeId}")
    @Operation(summary = "Delete knowledge source", 
               description = "Deletes a knowledge source from the system")
    public ResponseEntity<Void> delete(
            @PathVariable String agentId,
            @PathVariable String knowledgeId) {
        log.info("REST request to delete knowledge: {} for agent: {}", knowledgeId, agentId);
        
        agentKnowledgeService.delete(agentId, knowledgeId);
        
        return ResponseEntity.noContent().build();
    }
}
