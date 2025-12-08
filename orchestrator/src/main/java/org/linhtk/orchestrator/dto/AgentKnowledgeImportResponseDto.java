package org.linhtk.orchestrator.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgentKnowledgeImportResponseDto {
    private AgentKnowledgeResponseDto agentKnowledgeResponse;
    private List<String> fileNames;
    private int chunks = 0;
}
