package org.linhtk.orchestrator.dto;

import lombok.*;


/**
 * DTO for updating an existing conversation.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConversationUpdateRequestDto {
    private String name;
}
