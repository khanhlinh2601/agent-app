package org.linhtk.orchestrator.dto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatResponseDto {
    private ChatMessageResponseDto message;
    private ConversationResponseDto conversation;
}
