package org.linhtk.orchestrator.controller;

import org.linhtk.orchestrator.dto.KnowledgeChunkResponseDto;
import org.linhtk.orchestrator.service.KnowledgeChunkService;
import org.linhtk.orchestrator.service.KnowledgeImportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/knowledge/{knowledgeId}/chunks")
public class KnowledgeChunkController {
    private final KnowledgeChunkService  knowledgeChunkService;
    private final KnowledgeImportService knowledgeImportService;

    public KnowledgeChunkController(KnowledgeChunkService knowledgeChunkService, KnowledgeImportService knowledgeImportService) {
        this.knowledgeChunkService = knowledgeChunkService;
        this.knowledgeImportService = knowledgeImportService;
    }

    //create chunk
    @GetMapping
    public List<KnowledgeChunkResponseDto> getLists (@PathVariable String agentId, @PathVariable String knowledgeId) {
        return knowledgeChunkService.getByKnowledge(agentId, knowledgeId);
    }

    //update chunk
    //import file


    @GetMapping("/search")
    public List<KnowledgeChunkResponseDto> search (@PathVariable String agentId, @PathVariable String knowledgeId, @RequestParam String query, @RequestParam(defaultValue = "5") int topK) {
        return knowledgeChunkService.searchSimilarChunks(agentId, knowledgeId, query, topK);
    }

}
