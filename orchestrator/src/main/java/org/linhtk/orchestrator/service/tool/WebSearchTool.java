package org.linhtk.orchestrator.service.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Web search tool for AI agents.
 * Enables agents to search the web for information.
 */
@Component
@Slf4j
public class WebSearchTool implements Function<WebSearchTool.Request, WebSearchTool.Response> {

    public static final String TOOL_NAME = "web_search";

    /**
     * Request object for web search.
     */
    public record Request(
        @JsonProperty(required = true, value = "query")
        @JsonPropertyDescription("The search query to execute")
        String query,

        @JsonProperty(value = "max_results")
        @JsonPropertyDescription("Maximum number of results to return (default: 5)")
        Integer maxResults
    ) {
        public Request {
            if (maxResults == null) {
                maxResults = 5;
            }
        }
    }

    /**
     * Response object containing search results.
     */
    public record Response(
        String query,
        String results,
        int resultCount
    ) {}

    @Override
    public Response apply(Request request) {
        log.info("Executing web search for query: {}", request.query());

        try {
            // TODO: Implement actual web search integration
            // This is a placeholder implementation
            // You can integrate with APIs like:
            // - Google Custom Search API
            // - Bing Search API
            // - Tavily API
            // - Serper API

            String mockResults = """
                Search results for '%s':
                1. Sample result about %s - https://example.com/1
                2. Another result for %s - https://example.com/2
                3. More information on %s - https://example.com/3
                """.formatted(request.query(), request.query(), request.query(), request.query());

            return new Response(
                request.query(),
                mockResults.trim(),
                3
            );

        } catch (Exception e) {
            log.error("Error executing web search: {}", e.getMessage(), e);
            return new Response(
                request.query(),
                "Error performing search: " + e.getMessage(),
                0
            );
        }
    }
}

