package com.example.mcpgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaChatResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String role;
            private String content;
            private List<ToolCallResponse> tool_calls;

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

            public List<ToolCallResponse> getTool_calls() {
                return tool_calls;
            }

            public void setTool_calls(List<ToolCallResponse> tool_calls) {
                this.tool_calls = tool_calls;
            }
        }
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ToolCallResponse {
            private String id;
            private String type;
            private FunctionCall function;
            
            public String getId() {
                return id;
            }
            
            public void setId(String id) {
                this.id = id;
            }
            
            public String getType() {
                return type;
            }
            
            public void setType(String type) {
                this.type = type;
            }
            
            public FunctionCall getFunction() {
                return function;
            }
            
            public void setFunction(FunctionCall function) {
                this.function = function;
            }
        }
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class FunctionCall {
            private String name;
            private String arguments;
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public String getArguments() {
                return arguments;
            }
            
            public void setArguments(String arguments) {
                this.arguments = arguments;
            }
        }

        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    // Ollama native format
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageResponse {
        private String role;
        private String content;

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

    // OpenAI-compatible format
    private List<Choice> choices;
    
    // Ollama native format
    private MessageResponse message;
    private Boolean done;

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public MessageResponse getMessage() {
        return message;
    }

    public void setMessage(MessageResponse message) {
        this.message = message;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
