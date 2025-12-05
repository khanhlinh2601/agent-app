package org.linhtk.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.dto.AgentRequestDto;
import org.linhtk.orchestrator.dto.AgentResponseDto;
import org.linhtk.orchestrator.service.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for managing AI agents.
 * Provides endpoints for CRUD operations on agent configurations.
 * 
 * Endpoints:
 * - GET /api/agents - Retrieve all agents
 * - GET /api/agents/{agentId} - Retrieve specific agent
 * - POST /api/agents - Create new agent
 * - PUT /api/agents/{agentId} - Update existing agent
 */
@RestController
@RequestMapping("/api/agents")
@Slf4j
@Tag(name = "Agent Management", description = "APIs for managing AI agent configurations")
public class AgentController {
    
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Retrieves all agents in the system.
     * Returns a list of agent configurations without sensitive information like API keys.
     * 
     * @return ResponseEntity containing list of agents
     */
    @GetMapping
    @Operation(summary = "Get all agents", description = "Retrieves all AI agent configurations")
    public ResponseEntity<List<AgentResponseDto>> getAll() {
        log.info("REST request to get all agents");
        
        List<AgentResponseDto> agents = agentService.getAll();
        
        return ResponseEntity.ok(agents);
    }

    /**
     * Retrieves a specific agent by ID.
     * 
     * @param agentId The unique identifier of the agent
     * @return ResponseEntity containing agent details
     */
    @GetMapping("/{agentId}")
    @Operation(summary = "Get agent by ID", description = "Retrieves a specific agent configuration by ID")
    public ResponseEntity<AgentResponseDto> getById(@PathVariable String agentId) {
        log.info("REST request to get agent by ID: {}", agentId);
        
        AgentResponseDto agent = agentService.getById(agentId);
        
        return ResponseEntity.ok(agent);
    }

    /**
     * Creates a new agent with the provided configuration.
     * Validates request body and initializes AI models with provider settings.
     * 
     * @param requestDto The agent creation request containing configuration details
     * @return ResponseEntity containing created agent with HTTP 201 status
     */
    @PostMapping
    @Operation(summary = "Create new agent", description = "Creates a new AI agent with specified configuration")
    public ResponseEntity<AgentResponseDto> create(@Valid @RequestBody AgentRequestDto requestDto) {
        log.info("REST request to create agent: name={}, provider={}", 
                requestDto.getName(), requestDto.getProviderName());
        
        AgentResponseDto createdAgent = agentService.create(requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAgent);
    }

    /**
     * Updates an existing agent configuration.
     * Performs partial update - only fields present in request body are updated.
     * Clears model cache to ensure updated configuration is used.
     * 
     * @param agentId The unique identifier of the agent to update
     * @param requestDto The agent update request with new configuration values
     * @return ResponseEntity containing updated agent details
     */
    @PutMapping("/{agentId}")
    @Operation(summary = "Update agent", description = "Updates an existing agent configuration")
    public ResponseEntity<AgentResponseDto> update(
            @PathVariable String agentId,
            @Valid @RequestBody AgentRequestDto requestDto) {
        log.info("REST request to update agent: id={}", agentId);
        
        AgentResponseDto updatedAgent = agentService.update(agentId, requestDto);
        
        return ResponseEntity.ok(updatedAgent);
    }
}
