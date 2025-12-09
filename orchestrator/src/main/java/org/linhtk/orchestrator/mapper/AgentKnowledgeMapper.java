package org.linhtk.orchestrator.mapper;

import org.linhtk.common.mapper.EntityCreateUpdateMapper;
import org.linhtk.orchestrator.dto.AgentKnowledgeRequestDto;
import org.linhtk.orchestrator.dto.AgentKnowledgeResponseDto;
import org.linhtk.orchestrator.dto.FileKnowledgeImportConfigRequestDto;
import org.linhtk.orchestrator.model.knowledge.AgentKnowledge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for AgentKnowledge entity.
 * Handles mapping between AgentKnowledge entity and request/response DTOs.
 * Uses EntityCreateUpdateMapper as base interface for common mapping operations.
 */
@Mapper(componentModel = "spring")
public interface AgentKnowledgeMapper extends EntityCreateUpdateMapper<AgentKnowledge, AgentKnowledgeRequestDto, AgentKnowledgeResponseDto> {
    
    /**
     * Maps AgentKnowledge entity to response DTO.
     * 
     * @param agentKnowledge The agent knowledge entity
     * @return AgentKnowledgeResponseDto with all knowledge source details
     */
    @Override
    AgentKnowledgeResponseDto toVmResponse(AgentKnowledge agentKnowledge);
    
    /**
     * Maps FileKnowledgeImportConfigRequestDto to AgentKnowledge entity.
     * Used when creating knowledge from file uploads.
     * SourceType, agentId, and sourceUri will be set in service layer based on context.
     * Name and metadata are mapped from the request.
     * 
     * @param requestDto The file knowledge import request containing name and metadata
     * @return AgentKnowledge entity (partial mapping, requires agentId, sourceType, and sourceUri)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agentId", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "sourceUri", ignore = true)
    AgentKnowledge toEntity(FileKnowledgeImportConfigRequestDto requestDto);
}
