package org.linhtk.orchestrator.mapper;

import org.linhtk.orchestrator.dto.ConversationResponseDto;
import org.linhtk.orchestrator.model.conversation.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for converting between Conversation entity and ConversationResponseDto
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConversationMapper {
    
    /**
     * Converts Conversation entity to ConversationResponseDto
     * @param conversation The conversation entity
     * @return ConversationResponseDto
     */
    ConversationResponseDto toDto(Conversation conversation);
}
