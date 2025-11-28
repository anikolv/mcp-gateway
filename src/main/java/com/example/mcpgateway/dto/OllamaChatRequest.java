package com.example.mcpgateway.dto;

import java.util.List;
import java.util.Map;

public class OllamaChatRequest {

    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    private String model;
    private List<Message> messages;
    private List<Map<String, Object>> tools;
    private String tool_choice;
    private Boolean stream = false;

    public OllamaChatRequest() {}

    public OllamaChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Map<String, Object>> getTools() {
        return tools;
    }

    public void setTools(List<Map<String, Object>> tools) {
        this.tools = tools;
    }

    public String getTool_choice() {
        return tool_choice;
    }

    public void setTool_choice(String tool_choice) {
        this.tool_choice = tool_choice;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
