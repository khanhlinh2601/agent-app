package org.linhtk.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.linhtk.common.exception.NotFoundException;
import org.linhtk.orchestrator.constant.KnowledgeSourceType;
import org.linhtk.orchestrator.dto.AgentKnowledgeRequestDto;
import org.linhtk.orchestrator.dto.AgentKnowledgeResponseDto;
import org.linhtk.orchestrator.dto.FileKnowledgeImportConfigRequestDto;
import org.linhtk.orchestrator.mapper.AgentKnowledgeMapper;
import org.linhtk.orchestrator.model.knowledge.AgentKnowledge;
import org.linhtk.orchestrator.repository.AgentKnowledgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing agent knowledge sources.
 * Handles CRUD operations for knowledge sources associated with AI agents,
 * including file imports and general knowledge source management.
 * 
 * Design decisions:
 * - Validates agent ownership before any operation on knowledge
 * - Uses MapStruct for clean DTO mapping
 * - Implements proper transaction boundaries
 * - Follows SOLID principles with single responsibility
 */
@Service
@Slf4j
public class AgentKnowledgeService {
    
    private final AgentKnowledgeRepository agentKnowledgeRepository;
    private final AgentKnowledgeMapper agentKnowledgeMapper;

    public AgentKnowledgeService(AgentKnowledgeRepository agentKnowledgeRepository,
                                AgentKnowledgeMapper agentKnowledgeMapper) {
        this.agentKnowledgeRepository = agentKnowledgeRepository;
        this.agentKnowledgeMapper = agentKnowledgeMapper;
    }

    /**
     * Retrieves all knowledge sources for a specific agent.
     * Returns all knowledge sources without pagination.
     * 
     * @param agentId The unique identifier of the agent
     * @return List of all knowledge sources for the agent
     */
    public List<AgentKnowledgeResponseDto> getByAgent(String agentId) {
        log.debug("Retrieving all knowledge sources for agent: {}", agentId);
        
        List<AgentKnowledge> knowledgeSources = agentKnowledgeRepository.findByAgentId(agentId);
        
        log.info("Found {} knowledge sources for agent: {}", knowledgeSources.size(), agentId);
        
        return knowledgeSources.stream()
                .map(agentKnowledgeMapper::toVmResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific knowledge source for an agent.
     * Validates that the knowledge belongs to the specified agent.
     * 
     * @param agentId The unique identifier of the agent
     * @param knowledgeId The unique identifier of the knowledge source
     * @return AgentKnowledgeResponseDto containing knowledge details
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to the agent
     */
    public AgentKnowledgeResponseDto getOneForAgent(String agentId, String knowledgeId) {
        log.debug("Retrieving knowledge source: {} for agent: {}", knowledgeId, agentId);
        
        AgentKnowledge knowledge = findKnowledgeForAgent(agentId, knowledgeId);
        
        log.info("Successfully retrieved knowledge: {} for agent: {}", knowledgeId, agentId);
        
        return agentKnowledgeMapper.toVmResponse(knowledge);
    }

    /**
     * Creates a new knowledge source from file import configuration.
     * Used when importing knowledge from uploaded files.
     * SourceType is set to FILE by default.
     * 
     * Implementation notes:
     * - Sets sourceType to FILE automatically for file imports
     * - AgentId is set from path parameter for security
     * - Metadata from request is preserved for additional file information
     * 
     * @param agentId The unique identifier of the agent
     * @param request The file knowledge import configuration request
     * @return AgentKnowledgeResponseDto containing created knowledge details
     */
    @Transactional
    public AgentKnowledgeResponseDto create(String agentId, FileKnowledgeImportConfigRequestDto request) {
        log.info("Creating file knowledge for agent: {}, name: {}", agentId, request.getName());
        
        // Map request to entity (partial mapping)
        AgentKnowledge knowledge = agentKnowledgeMapper.toEntity(request);
        
        // Set required fields that come from path/context
        knowledge.setAgentId(agentId);
        knowledge.setSourceType(KnowledgeSourceType.FILE);
        // sourceUri will be set after file processing in controller/file service
        
        // Save knowledge to database
        AgentKnowledge savedKnowledge = agentKnowledgeRepository.save(knowledge);
        
        log.info("Successfully created file knowledge: {} for agent: {}", 
                savedKnowledge.getId(), agentId);
        
        return agentKnowledgeMapper.toVmResponse(savedKnowledge);
    }

    /**
     * Creates a new knowledge source with full configuration.
     * Used for general knowledge source creation (URL, DATABASE, etc.).
     * 
     * Implementation notes:
     * - Maps all fields from request DTO
     * - AgentId is set from path parameter for security
     * - Validates all required fields are present in request
     * 
     * @param agentId The unique identifier of the agent
     * @param request The agent knowledge request with full configuration
     * @return AgentKnowledgeResponseDto containing created knowledge details
     */
    @Transactional
    public AgentKnowledgeResponseDto create(String agentId, AgentKnowledgeRequestDto request) {
        log.info("Creating knowledge for agent: {}, name: {}, type: {}", 
                agentId, request.getName(), request.getSourceType());
        
        // Map request to entity
        AgentKnowledge knowledge = agentKnowledgeMapper.toModel(request);
        
        // Set agentId from path parameter for security (prevent knowledge injection)
        knowledge.setAgentId(agentId);
        
        // Save knowledge to database
        AgentKnowledge savedKnowledge = agentKnowledgeRepository.save(knowledge);
        
        log.info("Successfully created knowledge: {} for agent: {}", 
                savedKnowledge.getId(), agentId);
        
        return agentKnowledgeMapper.toVmResponse(savedKnowledge);
    }

    /**
     * Updates an existing knowledge source configuration.
     * Performs partial update - only non-null fields in request are updated.
     * Validates that knowledge belongs to the specified agent.
     * 
     * Implementation notes:
     * - Uses partial update strategy to preserve unchanged fields
     * - AgentId cannot be changed for security
     * - Validates knowledge ownership before update
     * 
     * @param agentId The unique identifier of the agent
     * @param knowledgeId The unique identifier of the knowledge source to update
     * @param request The knowledge update request with new values
     * @return AgentKnowledgeResponseDto containing updated knowledge details
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to the agent
     */
    @Transactional
    public AgentKnowledgeResponseDto update(String agentId, String knowledgeId, AgentKnowledgeRequestDto request) {
        log.info("Updating knowledge: {} for agent: {}", knowledgeId, agentId);
        
        // Retrieve existing knowledge and verify ownership
        AgentKnowledge existingKnowledge = findKnowledgeForAgent(agentId, knowledgeId);
        
        // Perform partial update using MapStruct
        // Only non-null fields in request will be updated
        agentKnowledgeMapper.partialUpdate(existingKnowledge, request);
        
        // Ensure agentId cannot be changed for security
        existingKnowledge.setAgentId(agentId);
        
        // Save updated knowledge
        AgentKnowledge updatedKnowledge = agentKnowledgeRepository.save(existingKnowledge);
        
        log.info("Successfully updated knowledge: {} for agent: {}", knowledgeId, agentId);
        
        return agentKnowledgeMapper.toVmResponse(updatedKnowledge);
    }

    /**
     * Deletes a knowledge source from the system.
     * Performs hard delete as soft delete is not required per specifications.
     * Validates that knowledge belongs to the specified agent.
     * 
     * Implementation notes:
     * - Performs hard delete (no soft delete required)
     * - Validates knowledge ownership before deletion
     * - Consider adding cleanup logic for associated files/embeddings if needed
     * 
     * @param agentId The unique identifier of the agent
     * @param knowledgeId The unique identifier of the knowledge source to delete
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to the agent
     */
    @Transactional
    public void delete(String agentId, String knowledgeId) {
        log.info("Deleting knowledge: {} for agent: {}", knowledgeId, agentId);
        
        // Retrieve existing knowledge and verify ownership
        AgentKnowledge knowledge = findKnowledgeForAgent(agentId, knowledgeId);
        
        // Perform hard delete
        agentKnowledgeRepository.delete(knowledge);
        
        log.info("Successfully deleted knowledge: {} for agent: {}", knowledgeId, agentId);
    }

    /**
     * Helper method to find knowledge by ID and verify agent ownership.
     * Centralizes knowledge retrieval and ownership validation logic.
     * 
     * @param agentId The agent identifier
     * @param knowledgeId The knowledge identifier
     * @return AgentKnowledge entity
     * @throws NotFoundException if knowledge doesn't exist or doesn't belong to the agent
     */
    private AgentKnowledge findKnowledgeForAgent(String agentId, String knowledgeId) {
        return agentKnowledgeRepository.findByAgentIdAndId(agentId, knowledgeId)
                .orElseThrow(() -> {
                    log.warn("Knowledge not found: {} for agent: {}", knowledgeId, agentId);
                    return new NotFoundException(
                            String.format("Knowledge not found with ID: %s for agent: %s", 
                                    knowledgeId, agentId));
                });
    }
}
