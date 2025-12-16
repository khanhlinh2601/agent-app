package org.linhtk.orchestrator.service.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Registry for managing AI agent tools.
 * Maps tool names to their implementations for dynamic tool loading.
 */
@Component
@Slf4j
public class ToolRegistry {

    private final Map<String, Function<?, ?>> tools = new HashMap<>();

    /**
     * Registers a tool with the given name and implementation.
     *
     * @param toolName The unique name of the tool
     * @param function The function implementation
     */
    public void registerTool(String toolName, Function<?, ?> function) {
        tools.put(toolName, function);
        log.info("Registered tool: {}", toolName);
    }

    /**
     * Retrieves a tool by name.
     *
     * @param toolName The name of the tool to retrieve
     * @return Optional containing the tool function if found
     */
    public Optional<Function<?, ?>> getTool(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }

    /**
     * Checks if a tool is registered.
     *
     * @param toolName The name of the tool
     * @return true if the tool is registered
     */
    public boolean hasToolRegistered(String toolName) {
        return tools.containsKey(toolName);
    }
}

