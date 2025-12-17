package org.linhtk.orchestrator.dto;

import lombok.*;

/**
 * DTO for creating a new conversation.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConversationCreateRequestDto {
    private String agentId;
    
    private String name;
}
