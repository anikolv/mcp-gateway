package com.example.mcpgateway.service;

import com.example.mcpgateway.dto.OllamaChatRequest;
import com.example.mcpgateway.dto.OllamaChatResponse;
import com.example.mcpgateway.dto.ToolCall;
import com.example.mcpgateway.dto.ToolResult;
import com.example.mcpgateway.mcp.MCPServerConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Service for interacting with LLM (DeepSeek via OpenRouter) with MCP tool support
 */
@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    @Value("${llm.model}")
    private String model;

    private final RestClient llmRestClient;
    private final MCPServerConfig mcpServerConfig;
    private final ToolDispatchService toolDispatchService;
    private final ObjectMapper objectMapper;

    public LLMService(
            @Qualifier("llmRestClient") RestClient llmRestClient,
            MCPServerConfig mcpServerConfig,
            ToolDispatchService toolDispatchService) {
        this.llmRestClient = llmRestClient;
        this.mcpServerConfig = mcpServerConfig;
        this.toolDispatchService = toolDispatchService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Main method to ask a question with MCP tool support
     */
    public String ask(String userQuestion) {
        try {
            log.info("Processing question: {}", userQuestion);

            List<OllamaChatRequest.Message> messages = new ArrayList<>();
            
            // Add system message with tool information
            String systemPrompt = "You MUST use the available tools when relevant. Never guess or make up exchange rate values. Always call the get_exchange_rates tool to fetch real data from the payment gateway.";
            messages.add(new OllamaChatRequest.Message("system", systemPrompt));
            
            // Add user question
            messages.add(new OllamaChatRequest.Message("user", userQuestion));

            // Call with tools
            OllamaChatRequest request = new OllamaChatRequest(model, messages);
            request.setTools(mcpServerConfig.getToolsAsOpenAIFormat());
            request.setTool_choice("auto");  // CRITICAL: Force tool calling
            
            log.info("Calling LLM with {} tools available and tool_choice=auto", mcpServerConfig.getTools().size());
            OllamaChatResponse parsedResponse = callLLMAndParse(request);
            
            if (parsedResponse == null) {
                log.error("Null response from LLM");
                return "Error: Received null response from LLM";
            }
            
            // First check for native OpenAI-style tool calls in the response object
            List<ToolCall> toolCalls = extractNativeToolCalls(parsedResponse);
            
            // Get the text content
            String responseText = extractMessageContent(parsedResponse, "(no response)");
            
            if (!toolCalls.isEmpty()) {
                log.info("Extracted {} tool call(s)", toolCalls.size());
                
                // Execute tool calls
                List<ToolResult> toolResults = toolDispatchService.dispatchAll(toolCalls);
                
                // Build a follow-up message with results
                StringBuilder resultsMessage = new StringBuilder("Here are the results from the payment gateway:\n\n");
                for (ToolResult result : toolResults) {
                    resultsMessage.append(result.isSuccess() ? result.getResult() : "Error: " + result.getError());
                    resultsMessage.append("\n\n");
                }
                resultsMessage.append("Please provide a clear, formatted answer based on these results.");
                
                // Ask LLM to format the final answer
                messages.add(new OllamaChatRequest.Message("assistant", responseText));
                messages.add(new OllamaChatRequest.Message("user", resultsMessage.toString()));
                
                OllamaChatRequest followUpRequest = new OllamaChatRequest(model, messages);
                // Don't send tools on follow-up
                OllamaChatResponse finalParsed = callLLMAndParse(followUpRequest);
                if (finalParsed != null) {
                    String finalResponse = extractMessageContent(finalParsed, responseText);
                    return finalResponse != null && !finalResponse.trim().isEmpty() ? finalResponse : responseText;
                }
                return responseText;
            }
            
            return responseText;
            
        } catch (Exception e) {
            log.error("Error in ask method", e);
            return "Error: " + e.getMessage();
        }
    }

    private String buildSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful AI assistant specialized in payment gateway operations.\n\n");
        
        prompt.append("AVAILABLE CAPABILITIES:\n");
        mcpServerConfig.getTools().forEach(tool -> {
            prompt.append(String.format("✓ %s: %s\n", tool.getName(), tool.getDescription()));
        });
        
        prompt.append("\nIMPORTANT INSTRUCTIONS:\n");
        prompt.append("1. You can ONLY help with the capabilities listed above (currency exchange rates from payment gateway).\n");
        prompt.append("2. When asked about things you CANNOT help with (transactions, deposits, user data, etc.), politely explain:\n");
        prompt.append("   - What you CANNOT do\n");
        prompt.append("   - What you CAN help with instead\n");
        prompt.append("3. To use a tool, respond with: TOOL_CALL: {\"name\": \"tool_name\", \"arguments\": {...}}\n");
        prompt.append("4. After receiving tool results, provide a clear, helpful answer.\n");
        prompt.append("5. Be friendly and suggest relevant queries the user could ask about exchange rates.\n");
        
        prompt.append("\nEXAMPLES OF WHAT YOU CAN HELP WITH:\n");
        prompt.append("- \"Is there an exchange rate for USD to EUR?\"\n");
        prompt.append("- \"What exchange rates are available from CAD?\"\n");
        prompt.append("- \"Show me all currency pairs\"\n");
        prompt.append("- \"What payment methods support EUR to USD conversion?\"\n");
        
        return prompt.toString();
    }

    private OllamaChatResponse callLLMAndParse(OllamaChatRequest request) {
        try {
            log.info("Calling OpenRouter API with model: {}", model);
            
            // OpenRouter uses OpenAI-compatible /chat/completions endpoint
            String responseBody = llmRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isEmpty()) {
                log.error("Empty response from LLM API");
                return null;
            }
            
            // Log first 500 chars of response for debugging
            log.info("Raw LLM response (first 500 chars): {}", 
                responseBody.substring(0, Math.min(500, responseBody.length())));

            // Parse response
            OllamaChatResponse response = objectMapper.readValue(responseBody, OllamaChatResponse.class);
            log.info("Successfully parsed response");
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calling LLM API", e);
            return null;
        }
    }

    private String extractMessageContent(OllamaChatResponse response, String rawResponse) {
        log.info("Extracting message from response");
        log.info("Response has choices: {}", response.getChoices() != null);
        
        // OpenRouter uses OpenAI-compatible format
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            log.info("Found {} choices", response.getChoices().size());
            OllamaChatResponse.Choice choice = response.getChoices().get(0);
            log.info("Choice is null: {}", choice == null);
            
            if (choice != null) {
                OllamaChatResponse.Choice.Message msg = choice.getMessage();
                log.info("Message is null: {}", msg == null);
                
                if (msg != null) {
                    log.info("Message role: {}, content: {}", 
                        msg.getRole(), 
                        msg.getContent() != null ? msg.getContent().substring(0, Math.min(100, msg.getContent().length())) : "NULL");
                    
                    if (msg.getContent() != null && !msg.getContent().isEmpty()) {
                        return msg.getContent();
                    } else {
                        return "DEBUG: Message content is " + (msg.getContent() == null ? "null" : "empty") + ". Raw response: " + rawResponse.substring(0, Math.min(300, rawResponse.length()));
                    }
                }
            }
        }
        
        log.warn("No valid content found in response. Raw: {}", rawResponse.substring(0, Math.min(300, rawResponse.length())));
        return "DEBUG: No choices found. Raw response: " + rawResponse.substring(0, Math.min(300, rawResponse.length()));
    }

    private List<ToolCall> extractNativeToolCalls(OllamaChatResponse response) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        try {
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                OllamaChatResponse.Choice.Message msg = response.getChoices().get(0).getMessage();
                if (msg != null && msg.getTool_calls() != null && !msg.getTool_calls().isEmpty()) {
                    log.info("Found {} native tool calls", msg.getTool_calls().size());
                    
                    for (OllamaChatResponse.Choice.ToolCallResponse toolCallResp : msg.getTool_calls()) {
                        if (toolCallResp.getFunction() != null) {
                            String name = toolCallResp.getFunction().getName();
                            String argumentsJson = toolCallResp.getFunction().getArguments();
                            
                            @SuppressWarnings("unchecked")
                            Map<String, Object> arguments = objectMapper.readValue(argumentsJson, Map.class);
                            
                            toolCalls.add(new ToolCall(toolCallResp.getId(), name, arguments));
                            log.info("Extracted native tool call: {} with args: {}", name, arguments);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract native tool calls", e);
        }
        
        return toolCalls;
    }
    
    private List<ToolCall> extractToolCalls(String response) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        if (response == null || !response.contains("TOOL_CALL:")) {
            return toolCalls;
        }
        
        try {
            // Extract all TOOL_CALL JSON objects from response
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains("TOOL_CALL:")) {
                    String jsonPart = line.substring(line.indexOf("{"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> toolCallData = (Map<String, Object>) objectMapper.readValue(jsonPart, Map.class);
                    
                    String name = (String) toolCallData.get("name");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> arguments = (Map<String, Object>) toolCallData.getOrDefault("arguments", Map.of());
                    
                    String id = "call_" + UUID.randomUUID().toString().substring(0, 8);
                    toolCalls.add(new ToolCall(id, name, arguments));
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse tool call from response", e);
        }
        
        return toolCalls;
    }
}

