package org.linhtk.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.linhtk.common.exception.NotFoundException;
import org.linhtk.orchestrator.dto.AgentRequestDto;
import org.linhtk.orchestrator.dto.AgentResponseDto;
import org.linhtk.orchestrator.mapper.AgentMapper;
import org.linhtk.orchestrator.model.agent.Agent;
import org.linhtk.orchestrator.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing AI agents.
 * Handles CRUD operations for agent configuration including provider settings,
 * model configuration, and publication status.
 * 
 * Design decisions:
 * - Clears model cache after create/update to ensure fresh configuration
 * - Uses MapStruct for clean DTO mapping
 * - Implements proper transaction boundaries
 * - Follows SOLID principles with single responsibility
 */
@Service
@Slf4j
public class AgentService {
    private final AgentRepository agentRepository;
    private final DynamicModelService dynamicModelService;
    private final AgentMapper agentMapper;

    public AgentService(AgentRepository agentRepository, 
                       DynamicModelService dynamicModelService,
                       AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.dynamicModelService = dynamicModelService;
        this.agentMapper = agentMapper;
    }

    /**
     * Retrieves all agents from the database.
     * Maps entities to response DTOs excluding sensitive information.
     * 
     * @return List of all agents as response DTOs
     */
    public List<AgentResponseDto> getAll() {
        log.debug("Retrieving all agents");
        
        List<Agent> agents = agentRepository.findAll();
        
        log.info("Found {} agents", agents.size());
        
        return agents.stream()
                .map(agentMapper::toVmResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific agent by ID.
     * 
     * @param agentId The agent identifier
     * @return AgentResponseDto containing agent details
     * @throws NotFoundException if agent doesn't exist
     */
    public AgentResponseDto getById(String agentId) {
        log.debug("Retrieving agent by ID: {}", agentId);
        
        Agent agent = findAgentById(agentId);
        
        log.info("Successfully retrieved agent: {}", agentId);
        
        return agentMapper.toVmResponse(agent);
    }

    /**
     * Creates a new agent with the provided configuration.
     * Validates provider settings and initializes AI models.
     * 
     * Implementation notes:
     * - Clears model cache to ensure fresh configuration
     * - Uses transactional boundary to ensure data consistency
     * - Maps request DTO to entity using MapStruct
     * 
     * @param requestDto The agent creation request
     * @return AgentResponseDto containing created agent details
     */
    @Transactional
    public AgentResponseDto create(AgentRequestDto requestDto) {
        log.info("Creating new agent: name={}, provider={}", 
                requestDto.getName(), requestDto.getProviderName());
        
        // Map request DTO to entity
        Agent agent = agentMapper.toModel(requestDto);
        
        // Save agent to database
        Agent savedAgent = agentRepository.save(agent);
        
        // Clear cache to ensure fresh model configuration when agent is used
        dynamicModelService.clearAgentCache(savedAgent.getId());
        
        log.info("Successfully created agent: id={}, name={}", 
                savedAgent.getId(), savedAgent.getName());
        
        return agentMapper.toVmResponse(savedAgent);
    }

    /**
     * Updates an existing agent with new configuration.
     * Performs partial update - only non-null fields in request are updated.
     * 
     * Implementation notes:
     * - Uses partial update strategy to preserve unchanged fields
     * - Clears model cache after update to ensure fresh configuration
     * - Validates agent existence before update
     * 
     * @param agentId The agent identifier to update
     * @param requestDto The agent update request with new values
     * @return AgentResponseDto containing updated agent details
     * @throws NotFoundException if agent doesn't exist
     */
    @Transactional
    public AgentResponseDto update(String agentId, AgentRequestDto requestDto) {
        log.info("Updating agent: id={}", agentId);
        
        // Retrieve existing agent
        Agent existingAgent = findAgentById(agentId);
        
        // Perform partial update using MapStruct
        // Only non-null fields in requestDto will be updated
        agentMapper.partialUpdate(existingAgent, requestDto);
        
        // Save updated agent
        Agent updatedAgent = agentRepository.save(existingAgent);
        
        // Clear cache to ensure fresh model configuration
        dynamicModelService.clearAgentCache(agentId);
        
        log.info("Successfully updated agent: id={}, name={}", 
                updatedAgent.getId(), updatedAgent.getName());
        
        return agentMapper.toVmResponse(updatedAgent);
    }

    /**
     * Helper method to find agent by ID with proper error handling.
     * Centralizes agent retrieval logic for reuse across service methods.
     * 
     * @param agentId The agent identifier
     * @return Agent entity
     * @throws NotFoundException if agent doesn't exist
     */
    private Agent findAgentById(String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> {
                    log.warn("Agent not found with ID: {}", agentId);
                    return new NotFoundException("Agent not found with ID: " + agentId);
                });
    }
}
