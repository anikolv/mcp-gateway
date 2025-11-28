package com.example.mcpgateway.service;

import com.example.mcpgateway.mcp.MCPServerConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service responsible for initializing and running the embedded MCP server
 */
@Service
public class MCPServerStarter {

    private static final Logger log = LoggerFactory.getLogger(MCPServerStarter.class);

    @Value("${mcp.enabled:true}")
    private boolean mcpEnabled;

    private final MCPServerConfig mcpServerConfig;

    public MCPServerStarter(MCPServerConfig mcpServerConfig) {
        this.mcpServerConfig = mcpServerConfig;
    }

    @PostConstruct
    public void startMCPServer() {
        if (!mcpEnabled) {
            log.info("MCP Server is disabled");
            return;
        }

        log.info("========================================");
        log.info("Starting MCP Server: {}", mcpServerConfig.getServerName());
        log.info("Version: {}", mcpServerConfig.getServerVersion());
        log.info("========================================");
        
        log.info("MCP Server initialized with {} tools:", mcpServerConfig.getTools().size());
        mcpServerConfig.getTools().forEach(tool -> {
            log.info("  ✓ {} - {}", tool.getName(), tool.getDescription());
        });
        
        log.info("========================================");
        log.info("MCP Server is ready!");
        log.info("Tools are available for AI assistant via /ask-ai endpoint");
        log.info("========================================");
    }

    public boolean isRunning() {
        return mcpEnabled;
    }

    public String getServerStatus() {
        if (!mcpEnabled) {
            return "MCP Server is disabled";
        }
        
        return String.format(
            "MCP Server '%s' (v%s) is running with %d tools available",
            mcpServerConfig.getServerName(),
            mcpServerConfig.getServerVersion(),
            mcpServerConfig.getTools().size()
        );
    }
}

