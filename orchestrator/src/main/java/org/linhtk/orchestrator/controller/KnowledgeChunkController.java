package org.linhtk.orchestrator.controller;

import org.linhtk.orchestrator.service.KnowledgeChunkService;
import org.linhtk.orchestrator.service.KnowledgeImportService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    //update chunk

    //import file


    //search similar chunks


}
