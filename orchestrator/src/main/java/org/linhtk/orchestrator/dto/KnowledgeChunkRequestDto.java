package org.linhtk.orchestrator.dto;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KnowledgeChunkRequestDto {
    private String content;
    private Map<String, Object> metadata;
}
