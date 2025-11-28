package com.example.mcpgateway.service;

import com.example.mcpgateway.dto.ToolCall;
import com.example.mcpgateway.dto.ToolResult;
import com.example.mcpgateway.mcp.MCPServerConfig;
import com.example.mcpgateway.mcp.MCPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for routing tool calls to appropriate microservices
 */
@Service
public class ToolDispatchService {

    private static final Logger log = LoggerFactory.getLogger(ToolDispatchService.class);

    private final MCPServerConfig mcpServerConfig;

    public ToolDispatchService(MCPServerConfig mcpServerConfig) {
        this.mcpServerConfig = mcpServerConfig;
    }

    /**
     * Dispatch a single tool call
     */
    public ToolResult dispatch(ToolCall toolCall) {
        log.info("Dispatching tool call: {} with id: {}", toolCall.getName(), toolCall.getId());
        
        try {
            MCPTool tool = mcpServerConfig.getTool(toolCall.getName());
            
            if (tool == null) {
                String error = "Tool not found: " + toolCall.getName();
                log.error(error);
                return ToolResult.error(toolCall.getId(), error);
            }
            
            String result = tool.execute(toolCall.getArguments());
            log.info("Tool {} executed successfully", toolCall.getName());
            
            return ToolResult.success(toolCall.getId(), result);
            
        } catch (Exception e) {
            log.error("Error dispatching tool call", e);
            return ToolResult.error(toolCall.getId(), "Error executing tool: " + e.getMessage());
        }
    }

    /**
     * Dispatch multiple tool calls
     */
    public List<ToolResult> dispatchAll(List<ToolCall> toolCalls) {
        log.info("Dispatching {} tool calls", toolCalls.size());
        
        List<ToolResult> results = new ArrayList<>();
        for (ToolCall toolCall : toolCalls) {
            results.add(dispatch(toolCall));
        }
        
        return results;
    }

    /**
     * Get list of available tools
     */
    public List<String> getAvailableTools() {
        return mcpServerConfig.getTools().stream()
                .map(MCPTool::getName)
                .toList();
    }
}

