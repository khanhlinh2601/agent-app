package org.linhtk.orchestrator.dto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessageResponseDto {
    private String id;
    private String content;
}
