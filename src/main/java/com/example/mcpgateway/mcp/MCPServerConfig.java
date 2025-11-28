package com.example.mcpgateway.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration for MCP Server - defines all available tools
 */
@Configuration
public class MCPServerConfig {

    private static final Logger log = LoggerFactory.getLogger(MCPServerConfig.class);

    @Value("${mcp.server.name:backend-services-mcp}")
    private String serverName;

    @Value("${mcp.server.version:1.0.0}")
    private String serverVersion;

    private final List<MCPTool> tools;

    public MCPServerConfig(List<MCPTool> tools) {
        this.tools = tools;
        log.info("Initialized MCP Server Config with {} tools", tools.size());
        tools.forEach(tool -> log.info("  - Registered tool: {}", tool.getName()));
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public List<MCPTool> getTools() {
        return tools;
    }

    public MCPTool getTool(String name) {
        return tools.stream()
                .filter(tool -> tool.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get tools in OpenAI function calling format for Ollama
     */
    public List<Map<String, Object>> getToolsAsOpenAIFormat() {
        return tools.stream()
                .map(tool -> Map.of(
                    "type", "function",
                    "function", Map.of(
                        "name", tool.getName(),
                        "description", tool.getDescription(),
                        "parameters", tool.getInputSchema()
                    )
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get MCP server information
     */
    public Map<String, Object> getServerInfo() {
        return Map.of(
            "name", serverName,
            "version", serverVersion,
            "protocol_version", "1.0",
            "capabilities", Map.of(
                "tools", Map.of(
                    "available", tools.size()
                )
            )
        );
    }
}

