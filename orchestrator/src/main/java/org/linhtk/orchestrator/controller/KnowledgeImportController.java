package org.linhtk.orchestrator.controller;

import org.linhtk.orchestrator.dto.AgentKnowledgeImportResponseDto;
import org.linhtk.orchestrator.dto.AgentKnowledgeResponseDto;
import org.linhtk.orchestrator.dto.FileKnowledgeImportConfigRequestDto;
import org.linhtk.orchestrator.dto.KnowledgeImportingResponseDto;
import org.linhtk.orchestrator.service.AgentKnowledgeService;
import org.linhtk.orchestrator.service.KnowledgeImportService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/knowledge")
public class KnowledgeImportController {
    private final AgentKnowledgeService agentKnowledgeService;
    private final KnowledgeImportService knowledgeImportService;

    public KnowledgeImportController(AgentKnowledgeService agentKnowledgeService, KnowledgeImportService knowledgeImportService) {
        this.agentKnowledgeService = agentKnowledgeService;
        this.knowledgeImportService = knowledgeImportService;
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public AgentKnowledgeImportResponseDto importFiles(@PathVariable String agentId,
                                                       @RequestPart List<MultipartFile> files,
                                                       @Valid @ModelAttribute FileKnowledgeImportConfigRequestDto configRequestDto) {
        var knowledge = agentKnowledgeService.create(agentId, configRequestDto);

        // Collect results first to avoid stream reuse
        var importResults = files.stream()
                .map(file -> knowledgeImportService.importDocument(agentId, knowledge.getId(), file))
                .toList();

        return AgentKnowledgeImportResponseDto.builder()
                .agentKnowledgeResponse(knowledge)
                .fileNames(importResults.stream()
                        .map(KnowledgeImportingResponseDto::getOriginalFilename)
                        .toList())
                .chunks(importResults.stream()
                        .mapToInt(KnowledgeImportingResponseDto::getNumberOfSegments)
                        .sum())
                .build();
    }
}
