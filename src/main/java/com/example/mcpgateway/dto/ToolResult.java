package com.example.mcpgateway.dto;

public class ToolResult {
    private String toolCallId;
    private String result;
    private boolean success;
    private String error;

    public ToolResult() {}

    public ToolResult(String toolCallId, String result, boolean success) {
        this.toolCallId = toolCallId;
        this.result = result;
        this.success = success;
    }

    public static ToolResult success(String toolCallId, String result) {
        return new ToolResult(toolCallId, result, true);
    }

    public static ToolResult error(String toolCallId, String error) {
        ToolResult toolResult = new ToolResult();
        toolResult.setToolCallId(toolCallId);
        toolResult.setError(error);
        toolResult.setSuccess(false);
        return toolResult;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

