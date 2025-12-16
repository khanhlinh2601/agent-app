package org.linhtk.orchestrator.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.service.tool.ToolRegistry;
import org.linhtk.orchestrator.service.tool.WebSearchTool;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for registering AI agent tools.
 * Auto-registers all available tools with the ToolRegistry on startup.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ToolConfiguration {

    private final ToolRegistry toolRegistry;
    private final WebSearchTool webSearchTool;

    /**
     * Registers all available tools with the registry.
     * Executed after bean initialization.
     */
    @PostConstruct
    public void registerTools() {
        log.info("Registering agent tools...");

        // Register web search tool
        toolRegistry.registerTool(WebSearchTool.TOOL_NAME, webSearchTool);

        // Add additional tools here as they are implemented

        log.info("Tool registration complete");
    }
}

