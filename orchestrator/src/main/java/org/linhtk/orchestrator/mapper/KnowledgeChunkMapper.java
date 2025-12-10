package org.linhtk.orchestrator.mapper;

import org.linhtk.common.mapper.EntityCreateUpdateMapper;
import org.linhtk.orchestrator.dto.KnowledgeChunkRequestDto;
import org.linhtk.orchestrator.dto.KnowledgeChunkResponseDto;
import org.linhtk.orchestrator.model.knowledge.KnowledgeChunk;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KnowledgeChunkMapper extends EntityCreateUpdateMapper<
        KnowledgeChunk, KnowledgeChunkResponseDto, KnowledgeChunkResponseDto> {

    @Override
    KnowledgeChunkResponseDto toVmResponse(KnowledgeChunk knowledgeChunk);

    @Mapping(target = "id", ignore = true)
    KnowledgeChunk toEntity(KnowledgeChunkRequestDto requestDto);
}
