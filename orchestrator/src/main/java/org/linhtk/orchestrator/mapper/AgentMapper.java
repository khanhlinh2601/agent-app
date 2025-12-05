package org.linhtk.orchestrator.mapper;

import org.linhtk.common.mapper.EntityCreateUpdateMapper;
import org.linhtk.orchestrator.dto.AgentRequestDto;
import org.linhtk.orchestrator.dto.AgentResponseDto;
import org.linhtk.orchestrator.model.agent.Agent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Agent entity.
 * Handles mapping between Agent entity and request/response DTOs.
 * Uses EntityCreateUpdateMapper as base interface for common mapping operations.
 */
@Mapper(componentModel = "spring")
public interface AgentMapper extends EntityCreateUpdateMapper<Agent, AgentRequestDto, AgentResponseDto> {
    
    /**
     * Maps Agent entity to response DTO.
     * Excludes sensitive information like API keys from response.
     * 
     * @param agent The agent entity
     * @return AgentResponseDto without sensitive fields
     */
    @Override
    AgentResponseDto toVmResponse(Agent agent);
}
