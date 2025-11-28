package com.example.mcpgateway.mcp;

import java.util.Map;

/**
 * Base interface for all MCP tools
 */
public interface MCPTool {
    
    /**
     * Get the unique name of this tool
     */
    String getName();
    
    /**
     * Get the description of what this tool does
     */
    String getDescription();
    
    /**
     * Get the JSON schema for the tool's input parameters
     */
    Map<String, Object> getInputSchema();
    
    /**
     * Execute the tool with the given arguments
     */
    String execute(Map<String, Object> arguments);
}

