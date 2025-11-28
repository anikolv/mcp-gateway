package com.example.mcpgateway.dto;

public class AskAiResponse {

    private String answer;

    public AskAiResponse() {
        this.answer = ""; // Default to empty string
    }

    public AskAiResponse(String answer) {
        this.answer = answer != null ? answer : "[NULL ANSWER FROM SERVICE]";
    }

    public String getAnswer() {
        return answer != null ? answer : "[NULL IN GETTER]";
    }

    public void setAnswer(String answer) {
        this.answer = answer != null ? answer : "[NULL IN SETTER]";
    }
}
