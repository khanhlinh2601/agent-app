package org.linhtk.orchestrator.mapper;

import org.linhtk.orchestrator.dto.ChatMessageResponseDto;
import org.linhtk.orchestrator.model.conversation.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for converting between ChatMessage entity and ChatMessageResponseDto
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMessageMapper {
    
    /**
     * Converts ChatMessage entity to ChatMessageResponseDto
     * @param chatMessage The chat message entity
     * @return ChatMessageResponseDto
     */
    ChatMessageResponseDto toDto(ChatMessage chatMessage);
}
