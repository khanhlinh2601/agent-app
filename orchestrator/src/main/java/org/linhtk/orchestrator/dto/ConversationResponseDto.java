package org.linhtk.orchestrator.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConversationResponseDto {
    private String id;
    private String name;
    private String agentId;
}
